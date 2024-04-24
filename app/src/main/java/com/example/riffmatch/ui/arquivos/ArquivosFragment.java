package com.example.riffmatch.ui.arquivos;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

//imports para file picker
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.example.riffmatch.databinding.FragmentArquivosBinding;

public class ArquivosFragment extends Fragment {

    private FragmentArquivosBinding binding;
    private byte[] MIDIfileData;

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

        binding.debugText.setText("AUDIO PER: "+ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_MEDIA_AUDIO"));
    }

    //Based on https://developer.android.com/training/permissions/requesting
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                } else {
                    Toast.makeText(getActivity(),"Permissão negada", Toast.LENGTH_LONG).show();
                }
            });

    private void enviarArquivo() {

    }

    //Based on https://www.youtube.com/watch?v=ZpJ66yzj8pM
    @Override public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
//        if (requestCode == 100 && resultCode == getActivity().RESULT_OK && data != null) {
//            Uri uri = data.getData();
//            String path = uri.getPath();
//            File file = new File(path);
//
//            binding.debugText.setText("info: "+file.getAbsolutePath());
//
//            //Based on https://stackoverflow.com/questions/21816049/write-and-read-binary-files-in-android
//            byte[] MIDIfileData = new byte[(int) file.length()];
//            DataInputStream dis = null;
//            try {
//                dis = new DataInputStream(new FileInputStream(file));
//            } catch (FileNotFoundException e) {
//                Toast.makeText(getActivity(),"Erro ao abrir o arquivo", Toast.LENGTH_LONG).show();
//                throw new RuntimeException(e);
//            }
//            try {
//                dis.readFully(MIDIfileData);
//            } catch (IOException e) {
//                Toast.makeText(getActivity(),"Erro ao ler o arquivo", Toast.LENGTH_LONG).show();
//                throw new RuntimeException(e);
//            }
//            try {
//                dis.close();
//            } catch (IOException e) {
//                Toast.makeText(getActivity(),"Erro ao fechar o arquivo", Toast.LENGTH_LONG).show();
//                throw new RuntimeException(e);
//            }
//
//
//        }

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
                Toast.makeText(getActivity(), "Erro ao ler o arquivo", Toast.LENGTH_LONG).show();
            }
            String MIDItext = new String(MIDIfileData, StandardCharsets.UTF_8);
            String[] MIDIparts = MIDItext.split("\n");
            StringBuilder hexString = new StringBuilder();

            for (char c : MIDIparts[0].toCharArray()) {
                hexString.append(String.format("%02X ", (int) c));
            }

            binding.debugText.setText(hexString.toString());

            Toast.makeText(getActivity(), MIDIparts[0], Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void procurarArquivo() {
        //Based on https://developer.android.com/training/permissions/requesting
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_MEDIA_AUDIO") == PackageManager.PERMISSION_DENIED)
        {
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
            Toast.makeText(getActivity(),"Por favor, instale um gerenciador de arquivos e tente de novo.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}