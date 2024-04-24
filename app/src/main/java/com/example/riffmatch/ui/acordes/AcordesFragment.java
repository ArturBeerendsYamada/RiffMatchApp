package com.example.riffmatch.ui.acordes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.riffmatch.databinding.FragmentAcordesBinding;

public class AcordesFragment extends Fragment {

    private FragmentAcordesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AcordesViewModel acordesViewModel =
                new ViewModelProvider(this).get(AcordesViewModel.class);

        binding = FragmentAcordesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        acordesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Listener para botao de acordes 1
        binding.buttonAcordes1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        //Listener para botao de acordes 2
        binding.buttonAcordes2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        //Listener para botao de acordes 3
        binding.buttonAcordes3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}