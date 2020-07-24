package com.vt.shoppet.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.firebase.AuthRepo
import com.vt.shoppet.firebase.FirestoreRepo
import com.vt.shoppet.livedata.DocumentLiveData
import com.vt.shoppet.livedata.QueryLiveData
import com.vt.shoppet.model.*

class DataViewModel @ViewModelInject constructor(
    private val auth: AuthRepo,
    private val firestore: FirestoreRepo,
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

    private val userLiveData by lazy {
        DocumentLiveData(firestore.getUserReference(auth.uid()))
    }
    private val petsLiveData by lazy {
        QueryLiveData(firestore.getPets())
    }
    private val starredPetsLiveData by lazy {
        QueryLiveData(firestore.getStarredPets())
    }
    private val ownPetsLiveData by lazy {
        QueryLiveData(firestore.getOwnPets())
    }
    private val chatsLiveData by lazy {
        QueryLiveData(firestore.getChats())
    }

    fun removeFirebaseData() {
        userLiveData.removeObservers(ProcessLifecycleOwner.get())
        petsLiveData.removeObservers(ProcessLifecycleOwner.get())
        starredPetsLiveData.removeObservers(ProcessLifecycleOwner.get())
        ownPetsLiveData.removeObservers(ProcessLifecycleOwner.get())
        chatsLiveData.removeObservers(ProcessLifecycleOwner.get())
    }

    fun initFirebaseData() {
        userLiveData.observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> _currentUser.value = result.data.toObject()
                is Result.Failure -> _currentUser.value = User()
            }
        }

        petsLiveData.observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> _pets.value = result.data.toObjects()
                is Result.Failure -> _pets.value = listOf()
            }
        }

        starredPetsLiveData.observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> _starredPets.value = result.data.toObjects()
                is Result.Failure -> _starredPets.value = listOf()
            }
        }

        ownPetsLiveData.observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> _ownPets.value = result.data.toObjects()
                is Result.Failure -> _ownPets.value = listOf()
            }
        }

        chatsLiveData.observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> _chats.value = result.data.toObjects()
                is Result.Failure -> _chats.value = listOf()
            }
        }

        handle.set("initFirebaseData", true)
    }

    init {
        resetFilter()
        handle.get<Boolean>("initFirebaseData")?.let { boolean ->
            if (boolean) initFirebaseData()
        }
    }

}