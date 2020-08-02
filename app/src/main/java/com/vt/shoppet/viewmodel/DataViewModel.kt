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
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.DataRepo
import com.vt.shoppet.util.localZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Instant
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class DataViewModel @ViewModelInject constructor(
    auth: AuthRepo,
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
    private val _unread = MutableLiveData<Int>()

    private val _filter = MutableLiveData<Filter>()

    val currentPet: LiveData<Pet> = _currentPet
    val currentUser: LiveData<User> = _currentUser
    val user: LiveData<User> = _user
    val chat: LiveData<Chat> = _chat

    val pets: LiveData<List<Pet>> = _pets
    val starredPets: LiveData<List<Pet>> = _starredPets
    val ownPets: LiveData<List<Pet>> = _ownPets
    val chats: LiveData<List<Chat>> = _chats
    val unread: LiveData<Int> = _unread

    var filteredPets: LiveData<List<Pet>> = MutableLiveData<List<Pet>>()
    val filter: LiveData<Filter> = _filter

    fun filterPets(filter: Filter) {
        _filter.value = filter
        filteredPets = petsLiveData.map { snapshots ->
            val pets: List<Pet> = snapshots.toObjects()
            val typeList =
                if (filter.type == "All") pets
                else pets.filter { it.type == filter.type }

            val sexList =
                if (filter.sex == "Both") typeList
                else typeList.filter { it.sex == filter.sex }

            val priceList =
                if (filter.price == "No Filter") sexList
                else sexList.filter { it.price in filter.amounts[0].toInt()..filter.amounts[1].toInt() }

            val now = LocalDateTime.now()

            val fromInstant = when (filter.age) {
                "Days" -> now.minusDays(filter.ages[0].toLong()).atZone(localZoneId)
                    .toInstant()
                "Weeks" -> now.minusWeeks(filter.ages[0].toLong()).atZone(localZoneId)
                    .toInstant()
                "Months" -> now.minusMonths(filter.ages[0].toLong()).atZone(localZoneId)
                    .toInstant()
                "Years" -> now.minusYears(filter.ages[0].toLong()).atZone(localZoneId)
                    .toInstant()
                else -> now.atZone(localZoneId).toInstant()
            }

            val toInstant = when (filter.age) {
                "Days" -> now.minusDays(filter.ages[1].toLong()).atZone(localZoneId)
                    .toInstant()
                "Weeks" -> now.minusWeeks(filter.ages[1].toLong()).atZone(localZoneId)
                    .toInstant()
                "Months" -> now.minusMonths(filter.ages[1].toLong()).atZone(localZoneId)
                    .toInstant()
                "Years" -> now.minusYears(filter.ages[1].toLong()).atZone(localZoneId)
                    .toInstant()
                else -> now.atZone(localZoneId).toInstant()
            }

            val ageList =
                if (filter.age == "No Filter") priceList
                else priceList.filter { Instant.ofEpochSecond(it.dateOfBirth.seconds) in toInstant..fromInstant }

            return@map when (filter.order) {
                "Ascending" -> when (filter.field) {
                    "Age" -> ageList.sortedByDescending { it.dateOfBirth }
                    "Breed" -> ageList.sortedBy { it.breed }
                    "Price" -> ageList.sortedBy { it.price }
                    "Type" -> ageList.sortedBy { it.type }
                    else -> ageList.sortedBy { it.date }
                }
                else -> when (filter.field) {
                    "Age" -> ageList.sortedBy { it.dateOfBirth }
                    "Breed" -> ageList.sortedByDescending { it.breed }
                    "Price" -> ageList.sortedByDescending { it.price }
                    "Type" -> ageList.sortedByDescending { it.type }
                    else -> ageList.sortedByDescending { it.date }
                }
            }
        }
    }

    fun resetFilter() {
        _filter.value = Filter()
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
        val chats: List<Chat> = snapshots.toObjects()
        _chats.value = chats
        var unread = 0
        chats.forEach { chat ->
            val index = chat.uid.indexOf(auth.uid())
            if (!chat.read[index]) unread++
        }
        _unread.value = unread
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