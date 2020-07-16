package sg.gov.tech.securebiometric;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


public class MainActivity extends AppCompatActivity {

    private String inputSecret;
    private boolean isLockFlag;
    Button secureInput_button;
    SecureBiometricManager secureBiometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isLockFlag = false;
        secureBiometricManager = new SecureBiometricManager(MainActivity.this, listener);

        secureInput_button = findViewById(R.id.secureButton);
        secureInput_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secureBiometricManager.showBiometricDialog();
            }
        });

        secureBiometricManager.checkBiometricIsAvailable();
    }

    BiometricListener listener = new BiometricListener() {
        @Override
        public void onSuccess(BiometricPrompt.AuthenticationResult result) {
            if (isLockFlag){
                //turn button text red
                secureInput_button.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                secureInput_button.setText("Encrypt Text");
                isLockFlag = false;

                // Decrypt data
            }
            else{
                //turn button text green
                secureInput_button.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                secureInput_button.setText("Decrypt Text");
                isLockFlag = true;

                // Encrypt data
                //encryptData(inputSecret, result);
            }

        }

        @Override
        public void onFailed() {
            String input = "Error in authenticating biometric";
            Log.e("MY_APP_TAG", input);
            Toast.makeText(MainActivity.this, input, Toast.LENGTH_LONG).show();
        }
    };

    private void encryptData(String inputSecret, BiometricPrompt.AuthenticationResult result) {
        byte[] encryptedInfo = new byte[0];
        try {
            encryptedInfo = result.getCryptoObject().getCipher().doFinal(
                    inputSecret.getBytes(Charset.defaultCharset()));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        Log.d("MY_APP_TAG", "Encrypted information: " +
                Arrays.toString(encryptedInfo));
    }

}
