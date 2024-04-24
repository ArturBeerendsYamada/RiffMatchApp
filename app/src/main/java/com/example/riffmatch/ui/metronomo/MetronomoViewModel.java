package com.example.riffmatch.ui.metronomo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MetronomoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MetronomoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is metronomo fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}