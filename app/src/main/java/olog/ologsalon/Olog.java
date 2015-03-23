package olog.ologsalon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.widget.AdapterView;

import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.FinishListener;
import com.google.zxing.client.android.camera.CameraManager;

/*
 * A base class for the Olog application. It's the main activity which is
 * launch at the beginning.
 *
 *
 * @author Groupe projet Olog
 */
public class Olog extends Activity implements SurfaceHolder.Callback {
    /*
     * Constante de temps au bout duquel on remet à 0 l'affichage.
     */
    private static final int       TEMPO         = 9;
    /*
     * Objet implémenté par ZXing, gère les images captées par la caméra.
     */
    private CameraManager          cameraManager;
    /*
     * Objet implémenté par ZXing, gère le décodage des QR Code.
     */
    private CaptureActivityHandler handler;
    /*
     * Vue gérant l'affichage des textes et de l'animation.
     *
     * @see olog.ologPut.affichage.Affichage
     *
     * @see olog.ologPut.affichage.InterfaceOlogAffichage
     */
    public Affichage               affichage;
    /*
     * Boolean indiquant si la vue nécessaire à l'affichage des images de fond
     * (captées par la caméra) est initialisée.
     */
    private boolean                hasSurface;
    /*
     * Compteur s'incrémentant lorsque l'on reçoit une image.
     */
    private int                    i             = 0;

    /*
     * Liste des images chargées
     */
    private HashMap<String,Bitmap> liste_images_initiale  = new HashMap<String,Bitmap>();
    private HashMap<String,Bitmap> liste_images  = new HashMap<String,Bitmap>();

    /*
        Objet à prendre et quantité
     */
    private String objet="";
    private int quantite=0;
    /*
    * OnClickListener pour la validation
     */
    private View.OnClickListener clickListenerAffichage = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Log.d("OnClick","Clic!!!");
            if(affichage.getTapEnable()){
                valider();
            }
        }
    };
    /*
     * getter de ecran.
     *
     * @return view which display the text
     */
    public Affichage getAffichage() {
        return affichage;
    }

    /*
     * getter de handler.
     *
     * @return handler which handle messages throw by the app
     */
    public Handler getHandler() {
        return handler;
    }

    /*
     * getter de cameraManager.
     *
     * @return CameraManager
     */
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    /*
     * Début du cycle de vie de l'application.
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Window win = getWindow();
        win.addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        winParams.flags |= 0x80000000;
        win.setAttributes( winParams );
        this.requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.capture );
        hasSurface = false;
        Log.i("OnCreate","Done!");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_olog, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restart:
                resetListeImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_olog, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restart:
                resetListeImage();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    /*
     * Appelée juste après onCreate ou lorsque l'on revient dans l'application
     * sans l'avoir killée.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate().
        // That led to bugs where the scanning rectangle was the wrong size and
        // partially
        // off screen.
        cameraManager = new CameraManager( getApplication(), this );

        // Initiate the views
        affichage = (Affichage) findViewById( R.id.viewfinder_view );

        handler = null;
        /*
         * On passe le cameraManager à l'affichage
         */
        ( (Affichage) affichage ).setCameraManager( cameraManager );
        ( (View) affichage ).setVisibility(View.VISIBLE);
        affichage.setOnClickListener(clickListenerAffichage);
        registerForContextMenu((View) affichage);
        /*
         * Envoie des objet nécessaire au fonctionnement de Zxing
         */

        SurfaceView surfaceView = (SurfaceView) findViewById( R.id.preview_view );
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        // Si surfaceHolder est initialisé on initialise la caméra, sinon on
        // attend
        if ( hasSurface ) {

            initCamera( surfaceHolder );
        }

        else {

            surfaceHolder.addCallback( this );
        }
        /*
         * Chargement des images
         */
        chargementDesImages();
        Log.d("OnResume","Done");
    }

    public void chargementDesImages(){
        chargementImage( "frisbee", R.drawable.frisbee_200 );
        chargementImage( "porte-clef", R.drawable.porte_clef_200 );
        chargementImage( "pins", R.drawable.pins_200 );
        chargementImage( "stylo", R.drawable.stylo_200 );
        chargementImage( "bracelet", R.drawable.bracelet_200 );
        liste_images=(HashMap<String,Bitmap>) liste_images_initiale.clone();
    }
    public void resetListeImage(){
        liste_images=(HashMap<String,Bitmap>) liste_images_initiale.clone();
    }
    /*
     * Gère le chargement des images à partir des ressources
     */
    private void chargementImage( String nom, int id ) {
        Bitmap bmp = BitmapFactory.decodeResource( getResources(), id );
        liste_images_initiale.put(nom, bmp);
        Log.d("chargementImage","Chargement d'une image");
    }
    /*
     * Appelée si l'on quitte l'application
     */
    @Override
    protected void onPause() {
        if ( handler != null ) {
            handler.quitSynchronously(); // on détruit le handler en libérant le
            // main

            handler = null;
        }

        cameraManager.closeDriver(); // on libère la main de la caméra
        if ( !hasSurface ) {
            SurfaceView surfaceView = (SurfaceView) findViewById( R.id.preview_view );
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback( this );
        }
        super.onPause();
    }

    /**
     * Appelée lorsque l'on kill l'app.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     *
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        if ( holder == null ) {
            Log.e( "Olog", "*** WARNING *** surfaceCreated() gave us a null surface!" );
        }
        if ( !hasSurface ) {
            hasSurface = true;
            initCamera( holder );
        }
    }

    /**
     *
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed( SurfaceHolder holder ) {
        hasSurface = false;
    }

    /*
     * Classe non utilisé ici
     *
     * @see
     * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
     * , int, int, int)
     */
    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {

    }
    public Boolean test( String resultat ) {
        Log.d("test","Début du test");
        // Parcours resultat en séparant aux endroits où il y a une vigule
        StringTokenizer st = new StringTokenizer( resultat, "," );
        String entete="";
        objet="";
        quantite=0;
        if ( st.hasMoreTokens() ) // s'il y a bien des ','
        {
            entete = st.nextToken(); // premier élément
            if ( entete.equals( "Olog" ) ) {
                Log.d("test","C'est bien un casier");
                // si c'est un casier
                try{

                    String tmp=st.nextToken();
                    tmp.replace(" ", "");
                    objet = tmp;
                    tmp=st.nextToken();
                    tmp.replace(" ", "");
                    quantite = Integer.parseInt(tmp);
                    Log.d("test","quantité : "+quantite);
                    Log.d("test","objet : "+objet);
                }
                catch(Exception ex){
                    entete="Casier";
                    objet="";
                    quantite=0;
                }
                return true;
            }
            else
                return false;

        }
        else
            return false;

    }
    /**
     * Appelée (par le handler) lorsque l'on a trouvé un QR Code.
     *
     * @param rawResult
     *            Le contenu du QR Code
     */
    public void handleDecode( Result rawResult ) {
        // Si on est devant l'un des casier où il faut déposer des articles
        if ( test(rawResult.getText()) )
        {
            // Sert à Afficher le nombre de produit à déposer dans le casier
            i=0;
            affichage.setQuantite(quantite);
            if(objet==""){
                affichage.setImage(null);
                i++;
            }
            else{
                affichage.setImage(liste_images.get(objet));
                i=0;
            }
            affichage.setMarker(rawResult);
            // on affiche la quantité à déposer
            affichage.draw();
        }
        restartPreviewAfterDelay(); // On rescanne
    }
    public void valider(){
        liste_images.put(objet,null);
        resetAffichage();
        if(listeImagesNul()){
            affichage.setCenterText("Mission accomplished ! \n Congratulations !");
            affichage.draw();
            SystemClock.sleep(5000);
        }
    }
    public boolean listeImagesNul(){
        Iterator i = liste_images.keySet().iterator();
        while(i.hasNext()){
            String key=(String) i.next();
            Bitmap image=liste_images.get(key);
            if(image!=null){
                return false;
            }
        }
        return true;
    }
    public void resetAffichage(){
        affichage.setCenterText("");
        affichage.setQuantite("");
        affichage.setImage(null);
        draw();
    }
    /*
     * Initialise le cameraManeger et le handler.
     *
     * @param surfaceHolder
     */
    private void initCamera( SurfaceHolder surfaceHolder ) {
        if ( surfaceHolder == null ) {
            throw new IllegalStateException( "No SurfaceHolder provided" );
        }
        try {
            cameraManager.openDriver( surfaceHolder );
            // Créé le handler
            if ( handler == null ) {
                handler = new CaptureActivityHandler( this, cameraManager );
            }
        } catch ( IOException ioe ) {
            Log.w( "Olog", ioe );
            displayFrameworkBugMessageAndExit();
        } catch ( RuntimeException e ) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w( "Olog", "Unexpected error initializing camera", e );
            displayFrameworkBugMessageAndExit();
        }
    }

    /*
     * Construit le message d'erreur et ferme l'application.
     */
    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Olog" );
        builder.setMessage( getString( R.string.msg_camera_framework_bug ) );
        builder.setPositiveButton( R.string.button_ok, new FinishListener( this ) );
        builder.setOnCancelListener( new FinishListener( this ) );
        builder.show();
    }

    /*
     * Reprise du scan des images entrantes, on n'a choisit zéro pour qu'il n'y
     * ait aucun délai.
     */
    public void restartPreviewAfterDelay() {
        if ( handler != null ) {
            handler.sendEmptyMessageDelayed( R.id.restart_preview, 0 );
        }
    }

    /*
     * Rafraichis la vue ecran.
     */
    public void draw() {
        affichage.draw();
    }

    /**
     * Appelée lorsque l'on reçoit une image de la caméra.
     */
    public void iteration() {
        i++;
        if ( i == TEMPO ) // au bout du tempo, on remet a zero l'affichage
        {
            resetAffichage();
        }
    }

}
