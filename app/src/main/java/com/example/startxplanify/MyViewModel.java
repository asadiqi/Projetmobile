package com.example.startxplanify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {

    private MutableLiveData<String> myData = new MutableLiveData<>();
    private MutableLiveData<Boolean> switchState = new MutableLiveData<>();
    public LiveData<String> getMyData() {
        return  myData;
    }

    public void setMyData(String data) {
        myData.setValue(data);
    }
}
