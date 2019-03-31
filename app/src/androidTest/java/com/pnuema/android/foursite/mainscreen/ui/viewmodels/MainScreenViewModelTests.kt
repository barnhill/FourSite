package com.pnuema.android.foursite.mainscreen.ui.viewmodels

import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.pnuema.android.foursite.mainscreen.ui.models.LocationResult
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

/**
 * Tests for MainScreenViewModel
 */
class MainScreenViewModelTests {
    lateinit var viewModel: MainScreenViewModel

    @Before
    fun setup() {
        viewModel = MainScreenViewModel()
        viewModel.locationResultsError = MutableLiveData()
        viewModel.locationResults = MutableLiveData()
    }

    @Test
    @Throws(Exception::class)
    fun test_setQuery() {
        viewModel.setQuery("query", mock(Location::class.java))

        assertEquals("query", viewModel.searchFilter)
    }

    @Test
    @Throws(Exception::class)
    fun test_refresh_EmptyQuery() {
        viewModel.locationResults = mock()
        viewModel.searchFilter = ""

        val observer = lambdaMock<(ArrayList<LocationResult>) -> Unit>()
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        viewModel.locationResults.observe({lifecycle}) {
            it?.let(observer)
        }

        viewModel.refresh(mock(Location::class.java))

        Mockito.verify(viewModel.locationResults, times(1)).postValue(Mockito.any())
    }

    private inline fun <reified T> lambdaMock(): T = mock(T::class.java)
    private inline fun <reified T: Any> mock() = mock(T::class.java)
}
