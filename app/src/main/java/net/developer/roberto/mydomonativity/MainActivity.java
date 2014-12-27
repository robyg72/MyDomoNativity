package net.developer.roberto.mydomonativity;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends ActionBarActivity {
    private SeekBar seekBarSpeed;
    private TextView textViewSpeed;
    private Switch switchWindMill;

    static final String windMillTag = "WindMill";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("WindMill", "Set Activity Title");
        setTitle("Controllo remoto presepe");
        // Retrieve the button id and set the event listener
        seekBarSpeed = (SeekBar) findViewById(R.id.seekBarSpeed);
        seekBarSpeed.setMax(255);
        seekBarSpeed.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarSpeed.setEnabled(false);

        textViewSpeed = (TextView) findViewById(R.id.textViewSpeed);
        textViewSpeed.setText("Velocità del Mulino: 0");

        switchWindMill = (Switch) findViewById(R.id.switchWindMill);
        switchWindMill.setChecked(false);
        switchWindMill.setText("Il Mulino è spento");
        switchWindMill.setOnCheckedChangeListener(switchCheckedListener);

        // Detect if WiFi is enabled
        Log.d(windMillTag, "Check if Wi-Fi is enabled");
        try {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            if (!wm.isWifiEnabled()) {
                Log.d(windMillTag, "Wi-Fi is disabled");
                DialogFragment dialog = new WiFiDialog();
                Bundle args = new Bundle();
                args.putString("title", "Verifica Wi-Fi");
                args.putString("message", "Attenzione: La rete Wi-Fi è disattivata");
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "MyDialogFragmentTag");
            }
        }
        catch (SecurityException se) {
            Log.d(windMillTag, se.getMessage());
            DialogFragment dialog = new WiFiDialog();
            Bundle args = new Bundle();
            args.putString("title", "Errore accesso Wi-Fi");
            args.putString("message", se.getMessage());
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), windMillTag);
        }
    }

    CompoundButton.OnCheckedChangeListener switchCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                switchWindMill.setText("Il Mulino è attivo");
                textViewSpeed.setText("Velocità del Mulino: 255");
                seekBarSpeed.setProgress(seekBarSpeed.getMax());
                seekBarSpeed.setEnabled(true);
            }
            else {
                switchWindMill.setText("Il Mulino è spento");
                textViewSpeed.setText("Velocità del Mulino: 0");
                seekBarSpeed.setProgress(0);
                seekBarSpeed.setEnabled(false);
            }
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int progressValue = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progressValue = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Toast.makeText(getApplicationContext(), "Modifica velocità del Mulino", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Toast.makeText(getApplicationContext(), "Velocità del Mulino variata", Toast.LENGTH_SHORT).show();
            textViewSpeed.setText("Velocità del Mulino: " + Integer.toString(progressValue));
            WindMillSocketServer wmss = new WindMillSocketServer("192.168.0.72", 125);
            wmss.execute(Integer.toString(progressValue));
        }
    };

    public class WindMillSocketServer extends AsyncTask<String, Integer, Integer> {
        String windMillAddress;
        int windMillPort;
        int response;

        WindMillSocketServer(String address, int port) {
            windMillAddress = address;
            windMillPort = port;
        }

        @Override
        protected Integer doInBackground(String... arg0) {
            Socket socket = null;
            DataOutputStream dos = null;

            try {
                socket = new Socket(windMillAddress, windMillPort);
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                //dos = new DataOutputStream(socket.getOutputStream());
                //dos.writeUTF(arg0[0]);
                pw.print(arg0[0] + "\n");
                pw.flush();
            }
            catch (UnknownHostException e) {
                Log.d(windMillTag, e.getMessage());
            }
            catch (IOException e) {
                Log.d(windMillTag, e.getMessage());
            }
            finally {
                if(dos != null)
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if(socket != null)
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return 0;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
