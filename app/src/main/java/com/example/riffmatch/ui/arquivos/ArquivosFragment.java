package com.example.riffmatch.ui.arquivos;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

//imports para file picker
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import com.example.riffmatch.MainActivity;
import com.example.riffmatch.MyBluetoothService;
import com.example.riffmatch.databinding.FragmentArquivosBinding;

public class ArquivosFragment extends Fragment {

    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private FragmentArquivosBinding binding;
    private byte[] MIDIfileData;
    private Activity toastActivity;
    private MyBluetoothService bluetoothService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ArquivosViewModel arquivosViewModel =
                new ViewModelProvider(this).get(ArquivosViewModel.class);

        binding = FragmentArquivosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Listener para botao de procurar arquivos no dispositivo
        binding.buttonProcurar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                procurarArquivo();
            }
        });

        //Listener para botao de enviasr arquivo para o RiffMatch por Bluetooth
        binding.buttonEnviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enviarArquivo();
            }
        });

        binding.debugText.setText("Nenhum arquivo carregado");
        toastActivity = getActivity();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Based on https://developer.android.com/training/permissions/requesting
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                } else {
                    Toast.makeText(toastActivity, "Permissão negada", Toast.LENGTH_LONG).show();
                }
            });

    //==================================================================================================================================================
    //                                                 PROCURAR ARQUIVOS - INICIO
    //==================================================================================================================================================

    //Based on https://www.youtube.com/watch?v=ZpJ66yzj8pM
    //Quando voltar da procura do arquivo
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK && data != null) {
            // Obtenha a URI do arquivo
            Uri uri = data.getData();
            // Baseado na URI, leia o conteúdo do arquivo
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                MIDIfileData = new byte[inputStream.available()];
                inputStream.read(MIDIfileData);
                inputStream.close();
                // Agora você pode manipular o conteúdo do arquivo MIDI
            } catch (IOException e) {
                // Trate os erros de leitura do arquivo aqui
                Toast.makeText(toastActivity, "Falha ao ler o arquivo", Toast.LENGTH_LONG).show();
            }

            String debugText = "Arquivo \"" + getFileName(uri) + "\" carregado.";
            binding.debugText.setText(debugText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void procurarArquivo() {
        //Based on https://developer.android.com/training/permissions/requesting
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_MEDIA_AUDIO") == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch("android.permission.READ_MEDIA_AUDIO");
            requestPermissionLauncher.launch("android.permission.READ_EXTERNAL_STORAGE");
        }

        //Based on https://www.youtube.com/watch?v=ZpJ66yzj8pM
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Selecione um Arquivo MIDI"), 100);
        } catch (Exception exception) {
            Toast.makeText(toastActivity, "Por favor, instale um gerenciador de arquivos e tente de novo.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    //==================================================================================================================================================
    //                                                 PROCURAR ARQUIVOS - FIM
    //==================================================================================================================================================

    //==================================================================================================================================================
    //                                                 ENVIAR ARQUIVOS - INICIO
    //==================================================================================================================================================

    private void enviarArquivo() {
        //Based on https://developer.android.com/training/permissions/requesting
                if (MIDIfileData == null) {
            Toast.makeText(toastActivity, "Nenhum arquivo carregado.", Toast.LENGTH_LONG).show();
            return;
        }

        if (    ContextCompat.checkSelfPermission(getContext(), "android.permission.BLUETOOTH") == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(getContext(), "android.permission.BLUETOOTH_ADMIN") == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(getContext(), "android.permission.BLUETOOTH_CONNECT") == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch("android.permission.BLUETOOTH");
            requestPermissionLauncher.launch("android.permission.BLUETOOTH_ADMIN");
            requestPermissionLauncher.launch("android.permission.BLUETOOTH_CONNECT");
        }
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            bluetoothService = activity.getBluetoothService();
        } else {
            throw new RuntimeException(context.toString()
                    + " must be MainActivity");
        }

        //Based on https://stackoverflow.com/questions/52229135/trying-to-get-bluetoothadapter-cannot-resolve-method-getsystemservicejava-lan
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Based on https://developer.android.com/develop/connectivity/bluetooth/setup
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(toastActivity, "Falha ao acessar bluetooth do aparelho", Toast.LENGTH_LONG).show();
        }
        //Based on https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice BTDeviceRiffmatchESP = null;
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().contains("Riffmatch")) {
                // Connect to the device
                BTDeviceRiffmatchESP = device;
                break;
            }
        }
        if (BTDeviceRiffmatchESP != null) {
            try {
                ConnectThread connectThread = new ConnectThread(BTDeviceRiffmatchESP);
                connectThread.start();

            } catch (Exception e) {
                Toast.makeText(toastActivity, "Bluetooth Connect Thread Failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(toastActivity, "Riffmatch não pareado", Toast.LENGTH_LONG).show();
        }

    }


    //Based on https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket = null;
        private final BluetoothDevice mmDevice;
        private static final String TAG = "MY_APP_DEBUG_TAG";

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch("android.permission.BLUETOOTH_CONNECT");
                    return;
                }
                tmp = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch("android.permission.BLUETOOTH_CONNECT");
                    return;
                }
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        MyBluetoothService.ConnectedThread connectedThread = bluetoothService.new ConnectedThread(mmSocket);
        connectedThread.start();

        String testMessage = "Hello World\n";
        String originalString = new String(MIDIfileData, StandardCharsets.UTF_8);
        int endOfHeader = originalString.indexOf("MTrk");
        String headerString = originalString.substring(0, endOfHeader);

        byte[] messageBytes = originalString.getBytes();
//        byte[] messageBytes = testMessage.getBytes();
        connectedThread.write(MIDIfileData);
    }

    //==================================================================================================================================================
    //                                                 ENVIAR ARQUIVOS - FIM
    //==================================================================================================================================================

    //Based on https://developer.android.com/develop/connectivity/bluetooth/transfer-data
//    public class MyBluetoothService {
//        private static final String TAG = "MY_APP_DEBUG_TAG";
//        private Handler handler; // handler that gets info from Bluetooth service
//
//        // Defines several constants used when transmitting messages between the
//        // service and the UI.
//
//        public static final int MESSAGE_READ = 0;
//        public static final int MESSAGE_WRITE = 1;
//        public static final int MESSAGE_TOAST = 2;
//
//            // ... (Add other message types here as needed.)
//
//        private class ConnectedThread extends Thread {
//            private final BluetoothSocket mmSocket;
//            private final InputStream mmInStream;
//            private final OutputStream mmOutStream;
//            private byte[] mmBuffer; // mmBuffer store for the stream
//
//            public ConnectedThread(BluetoothSocket socket) {
//                mmSocket = socket;
//                InputStream tmpIn = null;
//                OutputStream tmpOut = null;
//
//                // Get the input and output streams; using temp objects because
//                // member streams are final.
//                try {
//                    tmpIn = socket.getInputStream();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error occurred when creating input stream", e);
//                }
//                try {
//                    tmpOut = socket.getOutputStream();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error occurred when creating output stream", e);
//                }
//
//                mmInStream = tmpIn;
//                mmOutStream = tmpOut;
//            }
//
//            public void run() {
//                mmBuffer = new byte[1024];
//                int numBytes; // bytes returned from read()
//
//                // Keep listening to the InputStream until an exception occurs.
//                while (true) {
//                    try {
//                        // Read from the InputStream.
//                        numBytes = mmInStream.read(mmBuffer);
//                        // Send the obtained bytes to the UI activity.
//                        Message readMsg = handler.obtainMessage(
//                                MESSAGE_READ, numBytes, -1,
//                                mmBuffer);
//                        readMsg.sendToTarget();
//                    } catch (IOException e) {
//                        Log.d(TAG, "Input stream was disconnected", e);
//                        break;
//                    }
//                }
//            }
//
//            // Call this from the main activity to send data to the remote device.
//            public void write(byte[] bytes) {
//                try {
//                    mmOutStream.write(bytes);
//
//                    // Share the sent message with the UI activity.
//                    Message writtenMsg = handler.obtainMessage(
//                            MESSAGE_WRITE, -1, -1, mmBuffer);
//                    writtenMsg.sendToTarget();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error occurred when sending data", e);
//
//                    // Send a failure message back to the activity.
//                    Message writeErrorMsg =
//                            handler.obtainMessage(MESSAGE_TOAST);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("toast",
//                            "Couldn't send data to the other device");
//                    writeErrorMsg.setData(bundle);
//                    handler.sendMessage(writeErrorMsg);
//                }
//            }
//
//            // Call this method from the main activity to shut down the connection.
//            public void cancel() {
//                try {
//                    mmSocket.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "Could not close the connect socket", e);
//                }
//            }
//        }
//    }
//
//    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
//        MyBluetoothService.ConnectedThread connectedThread = bluetoothService.new ConnectedThread(mmSocket);
//        connectedThread.start();
//
//        String message = "Hello World";
//        String originalString = new String(MIDIfileData, StandardCharsets.UTF_8);
//        String[] splitStrings = originalString.split("\n");
//        byte[] messageBytes = splitStrings[0].getBytes();
//        connectedThread.write(messageBytes);
//    }

    //-------------- MIDI decoding --------------
    public class MIDIHeaderInfo {
        public int chunklen;
        public int format;
        public int ntracks;
        public int tickdiv;

        public MIDIHeaderInfo(){
            this.chunklen = -1;
            this.format = -1;
            this.ntracks = -1;
            this.tickdiv = -1;
        }
        public MIDIHeaderInfo(int c, int f, int n, int t){
            this.chunklen = c;
            this.format = f;
            this.ntracks = n;
            this.tickdiv = t;
        }
    }

    private MIDIHeaderInfo readMIDIHeader(byte[] MIDIChunk) {
        //itera sobre o arquivo procurando o header de cada chunk.
        //quando nao tem mais bytes no arquivo para ter o nome do header (4B) e o tamanho dele (4B) para a procura
        for (int i = 0; i <= MIDIChunk.length - 8; i++) {
            if (MIDIChunk[i] == 0x4D &&     //ascii hex 'M'
                MIDIChunk[++i] == 0x54 &&   //ascii hex 'T'
                MIDIChunk[++i] == 0x68 &&   //ascii hex 'h'
                MIDIChunk[++i] == 0x64      //ascii hex 'd'
            ){
                //se achou um header
                //soma os proximos 4 bytes (big-endian) para obter o comprimento da chunk)
                int chunklen =  (MIDIChunk[++i] << 8*3) +
                                (MIDIChunk[++i] << 8*2) +
                                (MIDIChunk[++i] << 8) +
                                MIDIChunk[++i];
                //soma os proximos 2 bytes (big-endian) para obter o formato do arquivo)
                int formato =   (MIDIChunk[++i] << 8) +
                                MIDIChunk[++i];
                //soma os proximos 2 bytes (big-endian) para obter o numero de tracks)
                int ntracks  =  (MIDIChunk[++i] << 8) +
                                MIDIChunk[++i];
                //soma os proximos 2 bytes (big-endian) para obter a informacao de tempo)
                int tickdiv =   (MIDIChunk[++i] << 8) +
                                MIDIChunk[++i];

                return new MIDIHeaderInfo(chunklen, formato, ntracks, tickdiv);
            }


        }
        //se nao encontrar headers pelo arquivo inteiro retorna -1 indicando arquivo invalido
        return new MIDIHeaderInfo();
    }
}
