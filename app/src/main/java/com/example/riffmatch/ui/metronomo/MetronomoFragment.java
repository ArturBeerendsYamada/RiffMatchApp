package com.example.riffmatch.ui.metronomo;

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

import com.example.riffmatch.databinding.FragmentMetronomoBinding;

public class MetronomoFragment extends Fragment {

    private FragmentMetronomoBinding binding;

    private TextView textMetronomo;
    private int metronomoBPM = 60;

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