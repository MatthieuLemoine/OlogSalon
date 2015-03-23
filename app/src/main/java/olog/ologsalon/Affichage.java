package olog.ologsalon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

/*
 * Classe qui s'occupe de l'affichage du texte et du clic.
 * Le texte peut s'afficher dans positions : LeftText, RightText, CenterText, PickText 
 */

public final class Affichage extends View {

    /*
     * String affiché en haut à droite.
     */
    private String        topRightText    = "";
    /*
     * String affiché en haut à gauche.
     */
    private String        topLeftText     = "";
    /*
     * String affiché au centre.
     */
    private String        centerText      = "";
    /*
     * Objet implémenté par ZXing, gère les images captées par la caméra.
     */
    private CameraManager cameraManager;
    /*
     * Context d'affichage de la vue.
     * 
     * @see android.graphics.Paint
     */
    private final Paint   paint;
    /*
     * String affiché au centre.
     */
    public String         bottomRightText = "";
    /*
     * Offset horizontal.
     */
    private int           frameLeft;
    /*
     * Offset vertical.
     */
    private int           frameTop;


    private boolean       initBackground;

    /*
     * Rectangle noir qui sert à caché l'affichage de la caméra
     */
    private Rect          rect_noir       = new Rect( 0, 0, 960, 550 );

    /*
     * Variable contenant la position du QR Code suivant l'axe x
     */
    private int           xCentreDuQRCode = 0;

    /*
     * Variable contenant la position du QR Code suivant l'axe y
     */
    private int           yCentreDuQRCode = 0;

    /*
     * Offset sur x corrigeant le fait que la caméra est située à droite des
     * lunettes et non au centre.
     */
    private int           offsetx         = 400;

    /*
     * Offset sur y corrigeant le fait que la caméra est située à droite des
     * lunettes et non au centre.
     */

    private int           offsety         = 200;

    /* Image du produit à afficher */
    private Bitmap        image           = null;

    /*
     * Matrice des points caractéristiques des QR Codes scannés.
     */
    private int[][]       marker = new int[2][3];

    public Bitmap getImage() {
        return image;
    }

    public void setImage( Bitmap image ) {
        this.image = image;
    }

    private String quantite="";

    private boolean tapEnable=false;
    public boolean getTapEnable(){
        return tapEnable;
    }
    public void setTapEnable(boolean bool){
        this.tapEnable=bool;
    }
    /*
     * Contructeur appelé par le fichier xml res/layout/capture.xml.
     * 
     * @param context
     * 
     * @param attrs
     */
    public Affichage( Context context, AttributeSet attrs ) {
        super( context, attrs );

        // Initialisation de l'objet paint et des ressources
        paint = new Paint( Paint.ANTI_ALIAS_FLAG );
        paint.setTextSize( (float) 25.0 );
        Resources resources = getResources();
        resources.getColor( R.color.viewfinder_mask );
        resources.getColor( R.color.result_view );
        resources.getColor( R.color.viewfinder_laser );
        resources.getColor( R.color.possible_result_points );
    }

    /*
     * Setter de cameraManager.
     * 
     * @param cameraManager
     */
    public void setCameraManager( CameraManager cameraManager ) {
        this.cameraManager = cameraManager;
    }

    /*
     * Appelé lorsque l'on rafraichit la vue.
     */
    @Override
    public void onDraw( Canvas canvas ) {
        Log.d("onDraw","Mise à jour de l'affichage");
        if ( cameraManager == null ) {
            return; /* L'application n'est pas encore prête */
        }

        /*
         * On récupère les rectangles qui représentent la surface d'affichage On
         * les utilise pour le positionnement du texte
         */
        Rect frame = cameraManager.getFramingRect();
        Log.i( "Affichage", "frame : " + frame );

        if ( frame == null ) {
            return; /* L'application n'est pas encore prête */
        }

        // Créé les variables contenant la hauteur et la largeur de l'écran
        frameLeft = frame.left;
        frameTop = frame.top;

        // Cache l'affichage de la caméra avec un rectangle noir
        paint.setColor( 0xFF000000 );
        canvas.drawRect( rect_noir, paint );

        // Permet d'afficher le texte en blanc
        paint.setColor( Color.WHITE );

        int x = frameLeft;
        int y = frameTop;

        // affichage de l'image
        
        if ( image != null ) {
            Log.d("onDraw","Image non nulle, draw de l'image");
            tapEnable=true;
            canvas.drawBitmap( image, 200, 200, null );
        }
        else if(image==null && quantite!="0" && quantite!=""){
            quantite="0";
        }
        else if(image==null && quantite==""){
            tapEnable=false;
        }
        // Affiche le texte contenue dans la variable topLeftText en haut à
        // gauche
        for ( String line : topLeftText.split( "\n" ) )
        {
            canvas.drawText( line, x + 50, y + 50, paint );
            y += paint.getFontSpacing();
        }

        // Affiche le texte contenue dans la variable topRightText en haut à
        // droite
        canvas.drawText( topRightText, frame.right - 400, frame.top + 50, paint );
        x = frame.right - 300;
        y = frame.top + 200;

        // Affiche le texte contenue dans la variable centerText au centre
        for ( String line : centerText.split( "\n" ) )
        {
            canvas.drawText( line, x, y, paint );
            y += paint.getFontSpacing();
        }

        // On agrandit la taille pour que le nombre de Put à effectuer soit plus
        // lisible
        paint.setTextSize( (float) 45.0 );

        // Affiche le texte contenue dans la variable numberText au centre
        for ( String line : quantite.split( "\n" ) )
        {
            Log.d("onDraw","Affichage de la quantité : "+quantite);
            canvas.drawText( line, xCentreDuQRCode + offsetx, yCentreDuQRCode + offsety, paint );
            y += paint.getFontSpacing();
        }

        // On remet la police à la taille précédente
        paint.setTextSize( (float) 25.0 );

        // Affiche le texte contenue dans la variable bottomRightText en bas à
        // droite
        canvas.drawText( bottomRightText, frame.right - 150, frame.bottom - 50, paint );
        
    }

    /*
     * Appelé pour rafraichir l'affichage.
     */
    public void draw() {
        invalidate();

    }

    /*
     * setter de topLeftText.
     */
    public void setLeftText( String s )
    {
        topLeftText = s;
    }

    /*
     * setter de topRightText.
     */
    public void setRightText( String casier )
    {
        topRightText = casier;
    }

    /*
     * setter de bottomRightText.
     */
    public void setPickAffiche( String pick )
    {
        bottomRightText = pick;
    }

    /*
     * setter de centerText.
     */
    public void setCenterText( String string ) {
        centerText = string;
    }

    /*
     * setter de marker
     *
     * @param result
     *            Informations du QR Code scanné.
     */
    public void setMarker( Result result ) {
        if ( cameraManager == null ) {
            return; /* L'application n'est pas encore prête */
        }
        Rect frame = cameraManager.getFramingRect();
        if ( frame == null ) {
            return; /* L'application n'est pas encore prête */
        }
        frameLeft = frame.left;
        frameTop = frame.top;
        /*
         * On récupère les markers définissant la position du QRCode
         */
        if ( result != null )
        {
            try {
                for ( int i = 0; i < 3; i++ )
                {
                    marker[0][i] = (int) frameLeft + ( (int) result.getResultPoints()[i].getX() );
                    marker[1][i] = (int) frameTop + ( (int) result.getResultPoints()[i].getY() );
                }

            } catch ( java.lang.Exception e ) {
                e.printStackTrace();
            }
        }
        else
            marker[0][0] = -1;

        //Récupère la position du centre du QR Code suivant l'axe x;
        xCentreDuQRCode=(marker[0][0]+marker[0][1])/2 ;

        //Récupère la position du centre du QR Code suivant l'axe x;
        yCentreDuQRCode=(marker[1][0]+marker[1][1])/2 ;
    }
    public void setQuantite(int quantite){
        this.quantite=Integer.toString(quantite);
    }
    public void setQuantite(String quantite){
        this.quantite=quantite;
    }
    /*
     * getter de bottomRightText.
     */
    public String getPick() {
        return bottomRightText;
    }
}
