package com.vt.shoppet.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Filter
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.DataRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class DataViewModel @ViewModelInject constructor(
    data: DataRepo,
    @Assisted private val handle: SavedStateHandle
) : ViewModel() {

    private val _currentPet = MutableLiveData<Pet>()
    private val _currentUser = MutableLiveData<User>()
    private val _user = MutableLiveData<User>()
    private val _chat = MutableLiveData<Chat>()

    private val _pets = MutableLiveData<List<Pet>>()
    private val _starredPets = MutableLiveData<List<Pet>>()
    private val _ownPets = MutableLiveData<List<Pet>>()
    private val _chats = MutableLiveData<List<Chat>>()

    private val _filteredPets = MutableLiveData<List<Pet>>()
    private val _filter = MutableLiveData<Filter>()

    val currentPet: LiveData<Pet> = _currentPet
    val currentUser: LiveData<User> = _currentUser
    val user: LiveData<User> = _user
    val chat: LiveData<Chat> = _chat

    val pets: LiveData<List<Pet>> = _pets
    val starredPets: LiveData<List<Pet>> = _starredPets
    val ownPets: LiveData<List<Pet>> = _ownPets
    val chats: LiveData<List<Chat>> = _chats

    val filteredPets: LiveData<List<Pet>> = _filteredPets
    val filter: LiveData<Filter> = _filter

    fun resetFilter() {
        _filter.value = Filter()
    }

    fun setFilteredPets(pets: List<Pet>) {
        _filteredPets.value = pets
    }

    fun setUser(user: User) {
        _user.value = user
    }

    fun setCurrentPet(pet: Pet) {
        _currentPet.value = pet
    }

    fun setChat(chat: Chat) {
        _chat.value = chat
    }

    private val userLiveData = data.currentUserFlow.asLiveData(Dispatchers.IO)
    private val petsLiveData = data.petsFlow.asLiveData(Dispatchers.IO)
    private val starredPetsLiveData = data.starredPetsFlow.asLiveData(Dispatchers.IO)
    private val ownPetsLiveData = data.ownPetsFlow.asLiveData(Dispatchers.IO)
    private val chatsLiveData = data.chatsFlow.asLiveData(Dispatchers.IO)

    private val userObserver = Observer<DocumentSnapshot> { document ->
        _currentUser.value = document.toObject()
    }
    private val petsObserver = Observer<QuerySnapshot> { snapshots ->
        _pets.value = snapshots.toObjects()
    }
    private val starredPetsObserver = Observer<QuerySnapshot> { snapshots ->
        _starredPets.value = snapshots.toObjects()
    }
    private val ownPetsObserver = Observer<QuerySnapshot> { snapshots ->
        _ownPets.value = snapshots.toObjects()
    }
    private val chatsObserver = Observer<QuerySnapshot> { snapshots ->
        _chats.value = snapshots.toObjects()
    }

    fun initFirebaseData() {
        userLiveData.observeForever(userObserver)
        petsLiveData.observeForever(petsObserver)
        starredPetsLiveData.observeForever(starredPetsObserver)
        ownPetsLiveData.observeForever(ownPetsObserver)
        chatsLiveData.observeForever(chatsObserver)
        handle.set("initFirebaseData", true)
    }

    fun removeFirebaseData() {
        userLiveData.removeObserver(userObserver)
        petsLiveData.removeObserver(petsObserver)
        starredPetsLiveData.removeObserver(starredPetsObserver)
        ownPetsLiveData.removeObserver(ownPetsObserver)
        chatsLiveData.removeObserver(chatsObserver)
        handle.remove<Boolean>("initFirebaseData")
    }

    private fun checkFirebaseData() {
        val initFirebaseData = handle.get<Boolean>("initFirebaseData") ?: return
        if (initFirebaseData) initFirebaseData()
    }

    init {
        resetFilter()
        checkFirebaseData()
    }

}