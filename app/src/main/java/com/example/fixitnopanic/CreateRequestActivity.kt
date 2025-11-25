package com.example.fixitnopanic

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class CreateRequestActivity : AppCompatActivity() {
    private lateinit var requestDao: RequestDao
    private lateinit var sharedPrefsHelper: SharedPrefsHelper

    private lateinit var editTextClientName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextModel: EditText
    private lateinit var editTextProblem: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var buttonCreateRequest: Button
    private lateinit var buttonBack: ImageButton

    private var currentRequestId: Long? = null
    private var isEditing = false

    private lateinit var countryCodeSpinner: Spinner
    private lateinit var countryFlagImageView: ImageView
    private lateinit var countries: List<Country>
    private var selectedCountry: Country? = null

    private var isFormatting = false
    private lateinit var phoneTextWatcher: TextWatcher
    private var previousLength = 0

    // Страны, для которых разрешено автоопределение при вводе
    private val autoDetectCountries = listOf(
        Country("Россия", "+7", R.drawable.russia_flag),
        Country("Казахстан", "+7", R.drawable.kazakhstan_flag),
        Country("Армения", "+374", R.drawable.armenia_flag),
        Country("Киргизия", "+996", R.drawable.kyrgyzstan_flag)
    )

    // Максимальное количество цифр для каждой страны
    private val maxDigitsMap = mapOf(
        "+7" to 10,        // Россия и Казахстан
        "+374" to 8,       // Армения
        "+375" to 9,       // Беларусь
        "+996" to 9,       // Киргизия
        "+998" to 9,       // Узбекистан
        "+992" to 9,       // Таджикистан
        "+995" to 9,       // Грузия
        "+972" to 9        // Израиль
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_request)

        requestDao = RequestDao(this)
        sharedPrefsHelper = SharedPrefsHelper(this)

        editTextClientName = findViewById(R.id.editTextClientName)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextModel = findViewById(R.id.editTextModel)
        editTextProblem = findViewById(R.id.editTextProblem)
        editTextDate = findViewById(R.id.editTextDate)
        editTextTime = findViewById(R.id.editTextTime)
        buttonCreateRequest = findViewById(R.id.buttonCreateRequest)
        buttonBack = findViewById(R.id.buttonBack)
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner)
        countryFlagImageView = findViewById(R.id.countryFlagImageView)



        val countrySelectorContainer = findViewById<LinearLayout>(R.id.countrySelectorContainer)
        countrySelectorContainer.setOnClickListener {
            countryCodeSpinner.performClick()
        }


        countries = listOf(
            Country(getString(R.string.country_select), "", R.drawable.question_mark_icon, true),
            Country("Россия", "+7", R.drawable.russia_flag),
            Country("Армения", "+374", R.drawable.armenia_flag),
            Country("Беларусь", "+375", R.drawable.belarus_flag),
            Country("Казахстан", "+7", R.drawable.kazakhstan_flag),
            Country("Киргизия", "+996", R.drawable.kyrgyzstan_flag),
            Country("Узбекистан", "+998", R.drawable.uzbekistan_flag),
            Country("Таджикистан", "+992", R.drawable.tajikistan_flag),
            Country("Грузия", "+995", R.drawable.georgia_flag),
            Country("Израиль", "+972", R.drawable.israel_flag)



        )

        // ✅ ИСПРАВЛЕННЫЙ АДАПТЕР ДЛЯ ВАШЕГО popup_item_country.xml
        val adapter = CountrySpinnerAdapter(this, countries)
        countryCodeSpinner.adapter = adapter

        phoneTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val input = s?.toString() ?: ""
                val currentLength = input.length

                // Специальная обработка для "+"
                if (input == "+" || input.startsWith("+ ") || input.startsWith("+  ")) {
                    safeSetPhoneText("+")
                    isFormatting = false
                    return
                }

                // Сброс при пустом поле
                val cleanDigits = input.filter { it.isDigit() || it == '+' }
                if (input.isEmpty() || (cleanDigits.none { it.isDigit() } && !input.contains("+"))) {
                    resetToInitialState()
                    isFormatting = false
                    previousLength = 0
                    return
                }

                val isDeleting = currentLength < previousLength
                previousLength = currentLength

                // Обработка определения страны
                handleCountryDetection(input, isDeleting)

                // Автоподстановка для России при вводе "7"
                if (input == "7" && (selectedCountry?.isDefault == true || selectedCountry == null) && !input.startsWith("+")) {
                    val russia = countries.find { it.code == "+7" && it.name == "Россия" }
                    if (russia != null) {
                        selectCountry(russia)
                        safeSetPhoneText("+7 ")
                        isFormatting = false
                        return
                    }
                }

                // Форматирование номера
                val formatted = formatPhoneNumber(input, isDeleting)
                safeSetPhoneText(formatted)
                isFormatting = false
            }
        }

        editTextPhone.addTextChangedListener(phoneTextWatcher)

        countryCodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) return
                selectCountry(countries[position])
                safeSetPhoneText("${countries[position].code} ")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        resetToInitialState()

        if (intent.hasExtra("request_id")) {
            isEditing = true
            currentRequestId = intent.getLongExtra("request_id", -1)
            if (currentRequestId != -1L) {
                loadRequestData(currentRequestId!!)
                buttonCreateRequest.text = getString(R.string.save_changes)
            } else {
                Toast.makeText(this, getString(R.string.edit_load_error), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        editTextDate.setOnClickListener { if (!isEditing) showDatePicker() }
        editTextTime.setOnClickListener { if (!isEditing) showTimePicker() }

        buttonCreateRequest.setOnClickListener {
            if (isEditing) updateRequest() else createRequest()
        }

        buttonBack.setOnClickListener {
            sharedPrefsHelper.setCurrentScreen("main")
            finish()
        }

        // ✅ ОБРАБОТЧИК СИСТЕМНОЙ КНОПКИ "НАЗАД" (свайп, кнопка внизу и т.д.)
        onBackPressedDispatcher.addCallback(this) {
            sharedPrefsHelper.setCurrentScreen("main")
            finish()
        }
    }

    // ✅ ИСПРАВЛЕННЫЙ АДАПТЕР ДЛЯ СПИННЕРА
    private class CountrySpinnerAdapter(
        private val context: Context,
        private val countries: List<Country>
    ) : BaseAdapter() {

        private val inflater = LayoutInflater.from(context)

        override fun getCount() = countries.size
        override fun getItem(position: Int) = countries[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.popup_item_country, parent, false)

            val country = countries[position]
            val flagImageView = view.findViewById<ImageView>(R.id.flagImageView)
            val countryNameTextView = view.findViewById<TextView>(R.id.countryNameTextView)

            // Устанавливаем флаг
            flagImageView.setImageResource(country.flag)

            // Устанавливаем название
            if (country.isDefault) {
                // Для "Выберите страну" показываем только текст
                countryNameTextView.text = country.name
            } else {
                // Для других стран показываем название и код
                countryNameTextView.text = "${country.name} (${country.code})"
            }

            return view
        }
    }

    private fun handleCountryDetection(input: String, isDeleting: Boolean) {
        // Если пользователь удаляет символы, сбрасываем страну только если удалил весь код
        if (isDeleting) {
            if (selectedCountry?.isDefault == false && !input.startsWith(selectedCountry!!.code)) {
                resetCountryState()
            }
            return
        }

        // Если ввод начинается с "+", проверяем на полное совпадение кода страны
        if (input.startsWith("+")) {
            // Сначала проверяем, не является ли текущая выбранная страна "специальной"
            val isCurrentCountrySpecial = autoDetectCountries.any {
                it.code == selectedCountry?.code && it.name == selectedCountry?.name
            }

            // Для специальных стран оставляем старую логику автоопределения
            if (isCurrentCountrySpecial) {
                handleSpecialCountryDetection(input)
                return
            }

            // Для остальных стран определяем страну ТОЛЬКО при полном совпадении кода
            handleRegularCountryDetection(input)
        }
    }

    private fun handleSpecialCountryDetection(input: String) {
        // Старая логика для специальных стран (Россия, Казахстан, Армения, Киргизия)
        var detectedCountry: Country? = null

        // Сначала пробуем найти точное совпадение
        for (country in countries) {
            if (country.isDefault) continue
            if (input.startsWith(country.code)) {
                detectedCountry = country
                break
            }
        }

        // Если не нашли точное совпадение, пробуем найти по префиксу
        if (detectedCountry == null) {
            val digitsAfterPlus = input.substring(1).filter { it.isDigit() }
            if (digitsAfterPlus.isNotEmpty()) {
                for (length in 3 downTo 1) {
                    if (digitsAfterPlus.length >= length) {
                        val prefix = "+" + digitsAfterPlus.substring(0, length)
                        detectedCountry = countries.find {
                            !it.isDefault && it.code.startsWith(prefix)
                        }
                        if (detectedCountry != null) break
                    }
                }
            }
        }

        if (detectedCountry != null) {
            selectCountry(detectedCountry)
        } else if (selectedCountry?.isDefault == false && input.length <= 4) {
            resetCountryState()
        }
    }

    private fun handleRegularCountryDetection(input: String) {
        // Для обычных стран определяем страну ТОЛЬКО при полном совпадении кода
        val exactMatchCountry = countries.find { country ->
            !country.isDefault &&
                    input.startsWith(country.code) &&
                    (input.length == country.code.length ||
                            (input.length > country.code.length && input[country.code.length].isDigit()))
        }

        if (exactMatchCountry != null) {
            // Устанавливаем страну только если она еще не установлена или это другая страна
            if (selectedCountry?.code != exactMatchCountry.code || selectedCountry?.name != exactMatchCountry.name) {
                selectCountry(exactMatchCountry)
            }
        } else if (!autoDetectCountries.any { it.code == selectedCountry?.code && it.name == selectedCountry?.name }) {
            // Сбрасываем страну, если текущая страна не специальная и не совпадает с введенным кодом
            if (selectedCountry?.isDefault == false && !input.startsWith(selectedCountry!!.code)) {
                resetCountryState()
            }
        }
    }

    private fun formatPhoneNumber(input: String, isDeleting: Boolean): String {
        if (isDeleting) return input

        val cleanInput = input.replace(" ", "")

        // Если страна еще не определена, просто возвращаем введенный текст
        if (selectedCountry?.isDefault == true) {
            return if (cleanInput.startsWith("+")) cleanInput else cleanInput.filter { it.isDigit() }
        }

        // Извлекаем код страны
        val countryCode = selectedCountry?.code ?: ""
        var localNumber = ""

        if (countryCode.isNotEmpty() && cleanInput.startsWith(countryCode)) {
            localNumber = cleanInput.substring(countryCode.length).filter { it.isDigit() }
        } else {
            localNumber = cleanInput.filter { it.isDigit() }
            // Если номер начинается с кода страны, но страна не выбрана, пытаемся определить
            if (localNumber.startsWith(countryCode.trim('+'))) {
                localNumber = localNumber.substring(countryCode.length - 1)
            }
        }

        // Получаем максимальное количество цифр для текущей страны
        val maxDigits = maxDigitsMap[selectedCountry?.code] ?: 15

        // Обрезаем лишние цифры
        if (localNumber.length > maxDigits) {
            localNumber = localNumber.substring(0, maxDigits)
        }

        return when (selectedCountry?.code) {
            "+7" -> {
                if (selectedCountry?.name == "Казахстан") {
                    if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatKZInternal(localNumber)}"
                } else {
                    if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatRUInternal(localNumber)}"
                }
            }
            "+374" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatAMInternal(localNumber)}"
            "+996" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatKGInternal(localNumber)}"
            "+375" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatBYInternal(localNumber)}"
            "+998" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatUZInternal(localNumber)}"
            "+992" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatTJInternal(localNumber)}"
            "+995" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatGEInternal(localNumber)}"
            "+972" -> if (localNumber.isEmpty()) "$countryCode " else "$countryCode ${formatILInternal(localNumber)}"
            else -> {
                if (localNumber.isEmpty()) "$countryCode " else "$countryCode $localNumber"
            }
        }
    }

    private fun resetToInitialState() {
        countryCodeSpinner.post {
            countryCodeSpinner.setSelection(0)
        }

        selectedCountry = countries.first()
        countryFlagImageView.setImageResource(R.drawable.question_mark_icon)
        updatePhoneHint()
        safeSetPhoneText("")
    }

    private fun resetCountryState() {
        selectedCountry = countries.first()
        countryFlagImageView.setImageResource(R.drawable.question_mark_icon)
        updatePhoneHint()
    }

    private fun selectCountry(country: Country) {
        selectedCountry = country

        countryCodeSpinner.post {
            val position = countries.indexOf(country)
            if (position > 0) {
                countryCodeSpinner.setSelection(position)
            }
        }

        countryFlagImageView.setImageResource(country.flag)
        updatePhoneHint()
    }

    private fun safeSetPhoneText(text: String) {
        editTextPhone.removeTextChangedListener(phoneTextWatcher)
        editTextPhone.setText(text)
        val safeCursor = text.length.coerceAtMost(editTextPhone.text?.length ?: 0)
        editTextPhone.setSelection(safeCursor)
        editTextPhone.addTextChangedListener(phoneTextWatcher)
    }

    private fun updatePhoneHint() {
        val country = selectedCountry
        val hint = when {
            country?.isDefault == true -> getString(R.string.phone_hint)
            country?.code == "+7" && country.name == "Казахстан" -> getString(R.string.hint_kazakhstan)
            country?.code == "+7" -> getString(R.string.hint_russia)
            country?.code == "+375" -> getString(R.string.hint_belarus)
            country?.code == "+374" -> getString(R.string.hint_armenia)
            country?.code == "+972" -> getString(R.string.hint_israel)
            country?.code == "+996" -> getString(R.string.hint_kyrgyzstan)
            country?.code == "+998" -> getString(R.string.hint_uzbekistan)
            country?.code == "+992" -> getString(R.string.hint_tajikistan)
            country?.code == "+995" -> getString(R.string.hint_georgia)
            else -> getString(R.string.phone_hint)
        }
        editTextPhone.hint = hint
    }

    // Форматы для каждой страны
    private fun formatRUInternal(digits: String): String {
        // +7 (xxx) xxx-xx-xx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(3, digits.length)))
                if (digits.length > 3) {
                    append(") ")
                    append(digits.substring(3, minOf(6, digits.length)))
                    if (digits.length > 6) {
                        append("-")
                        append(digits.substring(6, minOf(8, digits.length)))
                        if (digits.length > 8) {
                            append("-")
                            append(digits.substring(8, minOf(10, digits.length)))
                        }
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatKZInternal(digits: String): String {
        // +7 (xxx) xxx-xx-xx (формат Казахстана такой же как у России)
        return formatRUInternal(digits)
    }

    private fun formatBYInternal(digits: String): String {
        // +375 (xx) xxx-xx-xx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(2, digits.length)))
                if (digits.length > 2) {
                    append(") ")
                    append(digits.substring(2, minOf(5, digits.length)))
                    if (digits.length > 5) {
                        append("-")
                        append(digits.substring(5, minOf(7, digits.length)))
                        if (digits.length > 7) {
                            append("-")
                            append(digits.substring(7, minOf(9, digits.length)))
                        }
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatAMInternal(digits: String): String {
        // +374 (xx) xxx-xxx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(2, digits.length)))
                if (digits.length > 2) {
                    append(") ")
                    append(digits.substring(2, minOf(5, digits.length)))
                    if (digits.length > 5) {
                        append("-")
                        append(digits.substring(5, minOf(8, digits.length)))
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatILInternal(digits: String): String {
        // +972 (x) xxx-xxxx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(1, digits.length)))
                if (digits.length > 1) {
                    append(") ")
                    append(digits.substring(1, minOf(4, digits.length)))
                    if (digits.length > 4) {
                        append("-")
                        append(digits.substring(4, minOf(8, digits.length)))
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatKGInternal(digits: String): String {
        // +996 (xxx) xxx-xxx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(3, digits.length)))
                if (digits.length > 3) {
                    append(") ")
                    append(digits.substring(3, minOf(6, digits.length)))
                    if (digits.length > 6) {
                        append("-")
                        append(digits.substring(6, minOf(9, digits.length)))
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatUZInternal(digits: String): String {
        // +998 (xx) xxx-xx-xx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(2, digits.length)))
                if (digits.length > 2) {
                    append(") ")
                    append(digits.substring(2, minOf(5, digits.length)))
                    if (digits.length > 5) {
                        append("-")
                        append(digits.substring(5, minOf(7, digits.length)))
                        if (digits.length > 7) {
                            append("-")
                            append(digits.substring(7, minOf(9, digits.length)))
                        }
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatTJInternal(digits: String): String {
        // +992 (xx) xxx-xx-xx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(2, digits.length)))
                if (digits.length > 2) {
                    append(") ")
                    append(digits.substring(2, minOf(5, digits.length)))
                    if (digits.length > 5) {
                        append("-")
                        append(digits.substring(5, minOf(7, digits.length)))
                        if (digits.length > 7) {
                            append("-")
                            append(digits.substring(7, minOf(9, digits.length)))
                        }
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun formatGEInternal(digits: String): String {
        // +995 (xxx) xxx-xxx
        return buildString {
            if (digits.length >= 1) {
                append("(")
                append(digits.substring(0, minOf(3, digits.length)))
                if (digits.length > 3) {
                    append(") ")
                    append(digits.substring(3, minOf(6, digits.length)))
                    if (digits.length > 6) {
                        append("-")
                        append(digits.substring(6, minOf(9, digits.length)))
                    }
                }
            } else {
                append(digits)
            }
        }
    }

    private fun getMinDigitsForCountry(country: Country?): Int {
        return when (country?.code) {
            "+7" -> 10
            "+375" -> 9
            "+374" -> 8
            "+972" -> 9
            "+996" -> 9
            "+998" -> 9
            "+992" -> 9
            "+995" -> 9
            else -> 6
        }
    }

    private fun getMaxDigitsForCountry(country: Country?): Int {
        return maxDigitsMap[country?.code] ?: 15
    }

    private fun isValidPhoneNumber(phone: String, country: Country?): Boolean {
        if (phone.isEmpty()) return false
        val digits = phone.filter { it.isDigit() }
        val minDigits = getMinDigitsForCountry(country)
        val maxDigits = getMaxDigitsForCountry(country)
        return digits.length in minDigits..maxDigits
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", dayOfMonth, month + 1, year)
                editTextDate.setText(formattedDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = cal.timeInMillis
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                editTextTime.setText(formattedTime)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun createRequest() {
        val name = editTextClientName.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()
        val model = editTextModel.text.toString().trim()
        val problem = editTextProblem.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val time = editTextTime.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_client_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidPhoneNumber(phone, selectedCountry)) {
            Toast.makeText(this, getString(R.string.error_phone_invalid), Toast.LENGTH_SHORT).show()
            return
        }
        if (model.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_model), Toast.LENGTH_SHORT).show()
            return
        }
        if (problem.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_problem), Toast.LENGTH_SHORT).show()
            return
        }
        if (date.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_date), Toast.LENGTH_SHORT).show()
            return
        }
        if (time.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_time), Toast.LENGTH_SHORT).show()
            return
        }

        val id = requestDao.createRequestWithDateTime(name, phone, model, problem, "$date $time")
        if (id != -1L) {
            Toast.makeText(this, getString(R.string.request_created), Toast.LENGTH_SHORT).show()
            sharedPrefsHelper.setCurrentScreen("main")
            finish()
        } else {
            Toast.makeText(this, getString(R.string.request_creation_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRequestData(id: Long) {
        val req = requestDao.getAllRequests().find { it.id == id }
        if (req != null) {
            editTextClientName.setText(req.client)
            editTextModel.setText(req.model)
            editTextProblem.setText(req.problem)

            val dt = req.dateCreated
            if (dt.contains(" ")) {
                val parts = dt.split(" ", limit = 2)
                editTextDate.setText(parts[0])
                editTextTime.setText(parts[1])
            } else {
                editTextDate.setText(dt)
            }

            editTextDate.isEnabled = false
            editTextTime.isEnabled = false

            var matchedCountry: Country? = null
            val phone = req.phone

            // Сначала пробуем найти точное совпадение
            matchedCountry = countries.find { !it.isDefault && phone.startsWith(it.code) }

            // Особая логика для +7
            if (matchedCountry == null && phone.startsWith("+7")) {
                val digits = phone.substring(2).filter { it.isDigit() }.take(3)
                if (digits.startsWith("6") || digits.startsWith("7") || digits.startsWith("8")) {
                    matchedCountry = countries.find { it.code == "+7" && it.name == "Казахстан" }
                }
                matchedCountry = matchedCountry ?: countries.find { it.code == "+7" && it.name == "Россия" }
            }

            if (matchedCountry != null) {
                selectCountry(matchedCountry)
                safeSetPhoneText(phone)
            } else {
                resetToInitialState()
                safeSetPhoneText(phone)
            }
        } else {
            Toast.makeText(this, getString(R.string.request_not_found), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateRequest() {
        val id = currentRequestId ?: return
        val name = editTextClientName.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()
        val model = editTextModel.text.toString().trim()
        val problem = editTextProblem.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_client_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidPhoneNumber(phone, selectedCountry)) {
            Toast.makeText(this, getString(R.string.error_phone_invalid), Toast.LENGTH_SHORT).show()
            return
        }
        if (model.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_model), Toast.LENGTH_SHORT).show()
            return
        }
        if (problem.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_problem), Toast.LENGTH_SHORT).show()
            return
        }

        if (requestDao.updateRequestData(id, name, phone, model, problem) > 0) {
            Toast.makeText(this, getString(R.string.request_updated), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.request_update_error), Toast.LENGTH_SHORT).show()
        }
        sharedPrefsHelper.setCurrentScreen("main")
        finish()
    }
}
