package com.vt.shoppet.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.livedata.DocumentLiveData
import com.vt.shoppet.livedata.QueryLiveData
import com.vt.shoppet.model.*
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.util.zone
import java.time.Instant
import java.time.LocalDateTime

class DataViewModel @ViewModelInject constructor(
    private val auth: AuthRepo,
    private val firestore: FirestoreRepo,
    @Assisted private val handle: SavedStateHandle
) : ViewModel() {

    private val currentPet = MutableLiveData<Pet>()
    private val currentUser = handle.getLiveData<User>("currentUser")
    private val user = MutableLiveData<User>()
    private val chat = MutableLiveData<Chat>()
    private val pets = MutableLiveData<List<Pet>>()
    private val starredPets = MutableLiveData<List<Pet>>()
    private val ownPets = MutableLiveData<List<Pet>>()
    private val chats = MutableLiveData<List<Chat>>()
    private val filtered = MutableLiveData<List<Pet>>()
    private val filter = MutableLiveData(Filter())

    fun getPets(): LiveData<List<Pet>> = pets
    fun getOwnPets(): LiveData<List<Pet>> = ownPets
    fun getStarredPets(): LiveData<List<Pet>> = starredPets
    fun getChats(): LiveData<List<Chat>> = chats

    fun getFilteredPets(): LiveData<List<Pet>> = filtered

    fun filterPets() {
        pets.observe(ProcessLifecycleOwner.get()) { pets ->
            val filter = this.filter.value ?: return@observe

            val typeList =
                if (filter.type == "All") pets
                else pets.filter { it.type == filter.type }

            val sexList =
                if (filter.sex == "Both") typeList
                else typeList.filter { it.sex == filter.sex }

            val priceList =
                if (filter.price == "No Filter") sexList
                else sexList.filter { it.price in filter.amounts[0].toInt()..filter.amounts[1].toInt() }

            val fromInstant = when (filter.age) {
                "Days" -> LocalDateTime.now().minusDays(filter.ages[0].toLong()).atZone(zone)
                    .toInstant()
                "Weeks" -> LocalDateTime.now().minusWeeks(filter.ages[0].toLong()).atZone(zone)
                    .toInstant()
                "Months" -> LocalDateTime.now().minusMonths(filter.ages[0].toLong()).atZone(zone)
                    .toInstant()
                "Years" -> LocalDateTime.now().minusYears(filter.ages[0].toLong()).atZone(zone)
                    .toInstant()
                else -> LocalDateTime.now().atZone(zone).toInstant()
            }

            val toInstant = when (filter.age) {
                "Days" -> LocalDateTime.now().minusDays(filter.ages[1].toLong()).atZone(zone)
                    .toInstant()
                "Weeks" -> LocalDateTime.now().minusWeeks(filter.ages[1].toLong()).atZone(zone)
                    .toInstant()
                "Months" -> LocalDateTime.now().minusMonths(filter.ages[1].toLong()).atZone(zone)
                    .toInstant()
                "Years" -> LocalDateTime.now().minusYears(filter.ages[1].toLong()).atZone(zone)
                    .toInstant()
                else -> LocalDateTime.now().atZone(zone).toInstant()
            }

            val ageList =
                if (filter.age == "No Filter") priceList
                else priceList.filter { Instant.ofEpochSecond(it.dateOfBirth.seconds) in toInstant..fromInstant }

            val list = when (filter.order) {
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
            filtered.value = list
        }
    }

    fun getFilter(): LiveData<Filter> = filter

    fun resetFilter() {
        filter.value = Filter()
    }

    fun getCurrentUser(): LiveData<User> = currentUser

    fun getUser(): LiveData<User> = user

    fun setUser(user: User) {
        this.user.value = user
    }

    fun clearUser() {
        user.value = null
    }

    fun getCurrentPet(): LiveData<Pet> = currentPet

    fun setCurrentPet(pet: Pet) {
        currentPet.value = pet
    }

    fun getChat(): LiveData<Chat> = chat

    fun setChat(chat: Chat) {
        this.chat.value = chat
    }

    fun initFirebaseData() {
        DocumentLiveData(firestore.getUser(auth.uid())).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> currentUser.value = result.data.toObject()
            }
        }

        QueryLiveData(firestore.getPets()).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> pets.value = result.data.toObjects()
            }
        }

        QueryLiveData(firestore.getStarredPets()).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> starredPets.value = result.data.toObjects()
            }
        }

        QueryLiveData(firestore.getOwnPets()).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> ownPets.value = result.data.toObjects()
            }
        }

        QueryLiveData(firestore.getChats()).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> chats.value = result.data.toObjects()
            }
        }

        handle.set("initFirebaseData", true)
    }

    init {
        handle.get<Boolean>("initFirebaseData")?.let { boolean ->
            if (boolean) initFirebaseData()
        }
    }

}