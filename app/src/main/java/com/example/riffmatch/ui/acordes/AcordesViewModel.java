package com.example.riffmatch.ui.acordes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AcordesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AcordesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Pressione um dos botões acima para treinar acordes");
    }

    public LiveData<String> getText() {
        return mText;
    }
}