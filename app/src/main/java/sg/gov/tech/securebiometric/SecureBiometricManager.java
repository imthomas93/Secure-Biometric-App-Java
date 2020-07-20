package sg.gov.tech.securebiometric;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;


public class SecureBiometricManager {
    private Context context;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private BiometricManager biometricManager;
    private BiometricListener listener;
    private CryptographyManager cryptoManager;
    byte[] iv;


    SecureBiometricManager(Context context, BiometricListener listener){
        this.context = context;
        this.listener = listener;
        this.cryptoManager = new CryptographyManager();

}

    /**
     * This method is called when the onClick is called.
     *  It will first setup the biometric authentication dialog and
     * gets  an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [DECRYPT_MODE][Cipher.DECRYPT_MODE].
     * @param isLockFlag
     */
    void showBiometricDialog(boolean isLockFlag) {
        // Initialize everything needed for authentication
        setupBiometricPrompt();

        Cipher cipher = cryptoManager.getCipher();
        SecretKey secretKey = cryptoManager.getSecretKey();
        try {
            // Encrypt String
            if(!isLockFlag){
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }
            // Decrypt String
            else{
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            }
            // Prompt appears when user clicks authentication button.
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method checks if the device can support biometric authentication APIs
     */
    public void checkBiometricIsAvailable(){
        String input;
        biometricManager = BiometricManager.from(this.context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                input = "App can authenticate using biometrics.";
                Log.d("MY_APP_TAG", input);
                Toast.makeText(context, input, Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                input = "No biometric features available on this device.";
                Log.e("MY_APP_TAG", input);
                Toast.makeText(context, input, Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                input = "App can authenticate using biometrics.";
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                Toast.makeText(context, input, Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                input = "The user hasn't associated any biometric credentials with their account..";
                Log.e("MY_APP_TAG", input);
                Toast.makeText(context, input, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * This method setups the biometric authentication dialog
     */
    private void setupBiometricPrompt(){
        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt((FragmentActivity) context,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(context,
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                .show();
                        listener.onFailed();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                        super.onAuthenticationSucceeded(result);
                        listener.onSuccess(result);
                        // For decryption later on, we need to keep hold of the cipher's initialization vector
                        iv = result.getCryptoObject().getCipher().getIV();
                    }

                    @Override
                    public void onAuthenticationFailed(){
                        super.onAuthenticationFailed();
                        Toast.makeText(context, "Authentication failed",
                                Toast.LENGTH_SHORT)
                                .show();
                        listener.onFailed();

                    }
                });

        // Create prompt dialog
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for sample app")
                .setSubtitle("Log in using your biometric")
                .setNegativeButtonText("Use account password")
                // Allows device pin
                //.setDeviceCredentialAllowed(true)
                .build();

    }
}
