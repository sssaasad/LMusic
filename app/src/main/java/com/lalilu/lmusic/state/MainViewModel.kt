package com.lalilu.lmusic.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.domain.entity.LSong

class MainViewModel : ViewModel() {
    val nowPageInt: MutableLiveData<Int> = MutableLiveData(0)
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()
    val nowPlayingMusic: MutableLiveData<LSong> = MutableLiveData()
}