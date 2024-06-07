package com.example.riffmatch.ui.metronomo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.riffmatch.R;
import com.example.riffmatch.databinding.FragmentMetronomoBinding;

public class MetronomoFragment extends Fragment {

    private FragmentMetronomoBinding binding;

    private TextView textMetronomo;
    private static int metronomoBPM = 60;
    private boolean isPlaying = false;

    public void setMetronomoBPM(int new_value){
        metronomoBPM = new_value;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MetronomoViewModel metronomoViewModel = new ViewModelProvider(this).get(MetronomoViewModel.class);

        binding = FragmentMetronomoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textMetronomo = binding.textMetronomo;
        textMetronomo.setText(String.valueOf(metronomoBPM));
        binding.textBPM.setText("BPM");

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Listener para botao de aumentar volume
        binding.buttonAumentar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aumentarMetronomo();
            }
        });

        //Listener para botao de diminuir volume
        binding.buttonDiminuir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                diminuirMetronomo();
            }
        });

        //Listener para botao de play/pause
        binding.buttonPlaypause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isPlaying){
                    pausePlaying();
                }
                else {
                    startPlaying();
                }
            }
        });
    }

    private void startPlaying() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.me);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you

        isPlaying = true;
    }

    private void pausePlaying() {
        isPlaying = false;
    }

    private void diminuirMetronomo() {
        metronomoBPM--;
        // Display the new value in the text view.
        textMetronomo.setText(String.valueOf(metronomoBPM));
    }

    private void aumentarMetronomo() {
        metronomoBPM++;
        // Display the new value in the text view.
        textMetronomo.setText(String.valueOf(metronomoBPM));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}