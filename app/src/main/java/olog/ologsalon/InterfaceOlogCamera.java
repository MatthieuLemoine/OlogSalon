package olog.ologsalon;

import olog.ologsalon.Olog;

import com.google.zxing.client.android.camera.CameraConfigurationManager;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * Cette classe fait l'interface entre AuRore et la caméra du périphérique (implémentation pour android). 
 * À chaque fois qu'une image est captée, elle est envoyée à ZXing pour être décodée.
 * 
 * @author Groupe projet Olog
 */
public final class InterfaceOlogCamera implements Camera.PreviewCallback {

  /*
   * Objet implémenté par ZXing, gère la configuration de la caméra.
   */
  private final CameraConfigurationManager configManager;
  /*
   * Objet implémenté par ZXing, gère le décodage des QR Code.
   */
  private Handler previewHandler;
  /*
   * int passé en argument du message envoyé au handler.
   */
  private int previewMessage;
  
  private Olog parent;

  /*
   * Constructeur
   */
  public InterfaceOlogCamera(CameraConfigurationManager configManager,Olog olog) {
    this.configManager = configManager;
    this.parent=olog;
  }

  /*
   * Setter de handler
   */
  public void setHandler(Handler previewHandler, int previewMessage) {
    this.previewHandler = previewHandler;
    this.previewMessage = previewMessage;
  }
/*
 * Appelé à chaque fois qu'une image est reçue.
 */
  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    Point cameraResolution = configManager.getCameraResolution();
    Handler thePreviewHandler = previewHandler;
    if (cameraResolution != null && thePreviewHandler != null) {
      Message message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.x,
          cameraResolution.y, data); // on prépare le message à envoyé. Celui-ci contient la résolution de la caméra et l'image captée.
           message.sendToTarget(); // on envoie le message.
      parent.iteration(); // on notifie Aurore qu'une image a été reçue.
      previewHandler = null;
    } else {
    }
  }

}
