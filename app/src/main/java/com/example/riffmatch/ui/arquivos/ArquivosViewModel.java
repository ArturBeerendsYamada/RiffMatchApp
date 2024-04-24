package com.example.riffmatch.ui.arquivos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ArquivosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ArquivosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}