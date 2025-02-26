import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import viewmodel.ViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableLazyStateOf<T>(function: () -> T) : ReadWriteProperty<ViewModel, T> {
    private val state: MutableState<T> by lazy {
        mutableStateOf(function())
    }

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): T {
        return state.value
    }

    override fun setValue(thisRef: ViewModel, property: KProperty<*>, value: T) {
        state.value = value
    }

}

fun <T> mutableLazyStateOf(function: () -> T): MutableLazyStateOf<T> {
    return MutableLazyStateOf(function)
}
