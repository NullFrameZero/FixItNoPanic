package com.example.fixitnopanic;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateRequestActivity extends AppCompatActivity {

    private RequestDao requestDao;
    private SharedPrefsHelper sharedPrefsHelper;
    private EditText editTextClientName;
    private EditText editTextPhone;
    private EditText editTextModel;
    private EditText editTextProblem;
    private EditText editTextDate;
    private EditText editTextTime;
    private Button buttonCreateRequest;
    private ImageButton buttonBack;
    private Long currentRequestId = null;
    private boolean isEditing = false;
    private Spinner countryCodeSpinner;
    private ImageView countryFlagImageView;
    private List<Country> countries;
    private Country selectedCountry = null;
    private boolean isFormatting = false;
    private TextWatcher phoneTextWatcher;
    private int previousLength = 0;

    private final List<Country> autoDetectCountries = Arrays.asList(
            new Country("Россия", "+7", R.drawable.russia_flag),
            new Country("Казахстан", "+7", R.drawable.kazakhstan_flag),
            new Country("Армения", "+374", R.drawable.armenia_flag),
            new Country("Киргизия", "+996", R.drawable.kyrgyzstan_flag)
    );

    private final Map<String, Integer> maxDigitsMap = new HashMap<String, Integer>() {{
        put("+7", 10);
        put("+374", 8);
        put("+375", 9);
        put("+996", 9);
        put("+998", 9);
        put("+992", 9);
        put("+995", 9);
        put("+972", 9);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        requestDao = new RequestDao(this);
        sharedPrefsHelper = new SharedPrefsHelper(this);

        editTextClientName = findViewById(R.id.editTextClientName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextModel = findViewById(R.id.editTextModel);
        editTextProblem = findViewById(R.id.editTextProblem);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        buttonCreateRequest = findViewById(R.id.buttonCreateRequest);
        buttonBack = findViewById(R.id.buttonBack);
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner);
        countryFlagImageView = findViewById(R.id.countryFlagImageView);

        LinearLayout countrySelectorContainer = findViewById(R.id.countrySelectorContainer);
        countrySelectorContainer.setOnClickListener(v -> countryCodeSpinner.performClick());

        countries = Arrays.asList(
                new Country(getString(R.string.country_select), "", R.drawable.question_mark_icon, true),
                new Country("Россия", "+7", R.drawable.russia_flag),
                new Country("Армения", "+374", R.drawable.armenia_flag),
                new Country("Беларусь", "+375", R.drawable.belarus_flag),
                new Country("Казахстан", "+7", R.drawable.kazakhstan_flag),
                new Country("Киргизия", "+996", R.drawable.kyrgyzstan_flag),
                new Country("Узбекистан", "+998", R.drawable.uzbekistan_flag),
                new Country("Таджикистан", "+992", R.drawable.tajikistan_flag),
                new Country("Грузия", "+995", R.drawable.georgia_flag),
                new Country("Израиль", "+972", R.drawable.israel_flag)
        );

        CountrySpinnerAdapter adapter = new CountrySpinnerAdapter(this, countries);
        countryCodeSpinner.setAdapter(adapter);

        phoneTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = s == null ? 0 : s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String input = s == null ? "" : s.toString();
                int currentLength = input.length();

                if (input.equals("+") || input.startsWith("+ ") || input.startsWith("+  ")) {
                    safeSetPhoneText("+");
                    isFormatting = false;
                    return;
                }

                String cleanDigits = input.replaceAll("[^+\\d]", "");
                boolean hasDigit = false;
                for (int i = 0; i < cleanDigits.length(); i++) {
                    if (Character.isDigit(cleanDigits.charAt(i))) {
                        hasDigit = true;
                        break;
                    }
                }
                if (input.isEmpty() || (!hasDigit && !input.contains("+"))) {
                    resetToInitialState();
                    isFormatting = false;
                    previousLength = 0;
                    return;
                }

                boolean isDeleting = currentLength < previousLength;
                previousLength = currentLength;

                handleCountryDetection(input, isDeleting);

                if (input.equals("7") && (selectedCountry == null || selectedCountry.isDefault()) && !input.startsWith("+")) {
                    Country russia = null;
                    for (Country c : countries) {
                        if ("+7".equals(c.getCode()) && "Россия".equals(c.getName())) {
                            russia = c;
                            break;
                        }
                    }
                    if (russia != null) {
                        selectCountry(russia);
                        safeSetPhoneText("+7 ");
                        isFormatting = false;
                        return;
                    }
                }

                String formatted = formatPhoneNumber(input, isDeleting);
                safeSetPhoneText(formatted);
                isFormatting = false;
            }
        };

        editTextPhone.addTextChangedListener(phoneTextWatcher);

        countryCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                selectCountry(countries.get(position));
                safeSetPhoneText(countries.get(position).getCode() + " ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        resetToInitialState();

        if (getIntent().hasExtra("request_id")) {
            isEditing = true;
            currentRequestId = getIntent().getLongExtra("request_id", -1);
            if (currentRequestId != -1L) {
                loadRequestData(currentRequestId);
                buttonCreateRequest.setText(R.string.save_changes);
            } else {
                Toast.makeText(CreateRequestActivity.this, R.string.edit_load_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        editTextDate.setOnClickListener(v -> {
            if (!isEditing) showDatePicker();
        });
        editTextTime.setOnClickListener(v -> {
            if (!isEditing) showTimePicker();
        });

        buttonCreateRequest.setOnClickListener(v -> {
            if (isEditing) updateRequest(); else createRequest();
        });

        buttonBack.setOnClickListener(v -> {
            sharedPrefsHelper.setCurrentScreen("main");
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sharedPrefsHelper.setCurrentScreen("main");
                finish();
            }
        });
    }

    // === НОВЫЙ МЕТОД: автоопределение страны по номеру ===
    private Country detectCountryByPhone(String phone) {
        String clean = phone.replaceAll("[^+\\d]", "");
        for (Country c : countries) {
            if (c.isDefault()) continue;
            if (clean.startsWith(c.getCode())) {
                return c;
            }
        }
        // Для +7 — попытка различить РУ и КЗ по первым цифрам
        if (clean.startsWith("+7") && clean.length() >= 4) {
            String digits = clean.substring(2); // после +7
            if (digits.length() >= 3) {
                String firstThree = digits.substring(0, 3);
                if (firstThree.startsWith("6") || firstThree.startsWith("7") || firstThree.startsWith("8")) {
                    for (Country c : countries) {
                        if ("+7".equals(c.getCode()) && "Казахстан".equals(c.getName())) {
                            return c;
                        }
                    }
                }
            }
            for (Country c : countries) {
                if ("+7".equals(c.getCode()) && "Россия".equals(c.getName())) {
                    return c;
                }
            }
        }
        return countries.get(0); // default
    }

    private static class CountrySpinnerAdapter extends BaseAdapter {
        private final Context context;
        private final List<Country> countries;
        private final LayoutInflater inflater;

        public CountrySpinnerAdapter(Context context, List<Country> countries) {
            this.context = context;
            this.countries = countries;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return countries.size();
        }

        @Override
        public Object getItem(int position) {
            return countries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.popup_item_country, parent, false);
            }

            Country country = countries.get(position);
            ImageView flagImageView = view.findViewById(R.id.flagImageView);
            TextView countryNameTextView = view.findViewById(R.id.countryNameTextView);

            flagImageView.setImageResource(country.getFlag());

            if (country.isDefault()) {
                countryNameTextView.setText(country.getName());
            } else {
                countryNameTextView.setText(country.getName() + " (" + country.getCode() + ")");
            }

            return view;
        }
    }

    private void handleCountryDetection(String input, boolean isDeleting) {
        if (isDeleting) {
            if (selectedCountry != null && !selectedCountry.isDefault() && !input.startsWith(selectedCountry.getCode())) {
                resetCountryState();
            }
            return;
        }

        if (input.startsWith("+")) {
            boolean isCurrentCountrySpecial = false;
            if (selectedCountry != null) {
                for (Country c : autoDetectCountries) {
                    if (c.getCode().equals(selectedCountry.getCode()) && c.getName().equals(selectedCountry.getName())) {
                        isCurrentCountrySpecial = true;
                        break;
                    }
                }
            }

            if (isCurrentCountrySpecial) {
                handleSpecialCountryDetection(input);
            } else {
                handleRegularCountryDetection(input);
            }
        }
    }

    private void handleSpecialCountryDetection(String input) {
        Country detectedCountry = null;

        for (Country country : countries) {
            if (country.isDefault()) continue;
            if (input.startsWith(country.getCode())) {
                detectedCountry = country;
                break;
            }
        }

        if (detectedCountry == null) {
            String digitsAfterPlus = input.substring(1).replaceAll("[^\\d]", "");
            if (!digitsAfterPlus.isEmpty()) {
                for (int length = 3; length >= 1; length--) {
                    if (digitsAfterPlus.length() >= length) {
                        String prefix = "+" + digitsAfterPlus.substring(0, length);
                        for (Country country : countries) {
                            if (!country.isDefault() && country.getCode().startsWith(prefix)) {
                                detectedCountry = country;
                                break;
                            }
                        }
                        if (detectedCountry != null) break;
                    }
                }
            }
        }

        if (detectedCountry != null) {
            selectCountry(detectedCountry);
        } else if (selectedCountry != null && !selectedCountry.isDefault() && input.length() <= 4) {
            resetCountryState();
        }
    }

    private void handleRegularCountryDetection(String input) {
        Country exactMatchCountry = null;
        for (Country country : countries) {
            if (country.isDefault()) continue;
            if (input.startsWith(country.getCode())) {
                if (input.length() == country.getCode().length() ||
                        (input.length() > country.getCode().length() && Character.isDigit(input.charAt(country.getCode().length())))) {
                    exactMatchCountry = country;
                    break;
                }
            }
        }

        if (exactMatchCountry != null) {
            if (!exactMatchCountry.getCode().equals(selectedCountry != null ? selectedCountry.getCode() : null) ||
                    !exactMatchCountry.getName().equals(selectedCountry != null ? selectedCountry.getName() : null)) {
                selectCountry(exactMatchCountry);
            }
        } else if (selectedCountry != null && !selectedCountry.isDefault()) {
            boolean isSpecial = false;
            for (Country c : autoDetectCountries) {
                if (c.getCode().equals(selectedCountry.getCode()) && c.getName().equals(selectedCountry.getName())) {
                    isSpecial = true;
                    break;
                }
            }
            if (!isSpecial && !input.startsWith(selectedCountry.getCode())) {
                resetCountryState();
            }
        }
    }

    private String formatPhoneNumber(String input, boolean isDeleting) {
        if (isDeleting) return input;
        String cleanInput = input.replace(" ", "");

        if (selectedCountry != null && selectedCountry.isDefault()) {
            return cleanInput.startsWith("+") ? cleanInput : cleanInput.replaceAll("[^\\d]", "");
        }

        String countryCode = selectedCountry != null ? selectedCountry.getCode() : "";
        String localNumber;

        if (!countryCode.isEmpty() && cleanInput.startsWith(countryCode)) {
            localNumber = cleanInput.substring(countryCode.length()).replaceAll("[^\\d]", "");
        } else {
            localNumber = cleanInput.replaceAll("[^\\d]", "");
            if (localNumber.startsWith(countryCode.replace("+", "")) && !countryCode.isEmpty()) {
                localNumber = localNumber.substring(countryCode.length() - 1);
            }
        }

        Integer maxDigitsObj = maxDigitsMap.get(countryCode);
        int maxDigits = (maxDigitsObj != null) ? maxDigitsObj : 15;

        if (localNumber.length() > maxDigits) {
            localNumber = localNumber.substring(0, maxDigits);
        }

        if ("+7".equals(countryCode)) {
            if ("Казахстан".equals(selectedCountry != null ? selectedCountry.getName() : null)) {
                return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatKZInternal(localNumber);
            } else {
                return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatRUInternal(localNumber);
            }
        } else if ("+374".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatAMInternal(localNumber);
        } else if ("+996".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatKGInternal(localNumber);
        } else if ("+375".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatBYInternal(localNumber);
        } else if ("+998".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatUZInternal(localNumber);
        } else if ("+992".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatTJInternal(localNumber);
        } else if ("+995".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatGEInternal(localNumber);
        } else if ("+972".equals(countryCode)) {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + formatILInternal(localNumber);
        } else {
            return localNumber.isEmpty() ? countryCode + " " : countryCode + " " + localNumber;
        }
    }

    private void resetToInitialState() {
        countryCodeSpinner.post(() -> countryCodeSpinner.setSelection(0));
        selectedCountry = countries.get(0);
        countryFlagImageView.setImageResource(R.drawable.question_mark_icon);
        updatePhoneHint();
        safeSetPhoneText("");
    }

    private void resetCountryState() {
        selectedCountry = countries.get(0);
        countryFlagImageView.setImageResource(R.drawable.question_mark_icon);
        updatePhoneHint();
    }

    private void selectCountry(Country country) {
        selectedCountry = country;
        countryCodeSpinner.post(() -> {
            int position = countries.indexOf(country);
            if (position > 0) {
                countryCodeSpinner.setSelection(position);
            }
        });
        countryFlagImageView.setImageResource(country.getFlag());
        updatePhoneHint();
    }

    private void safeSetPhoneText(String text) {
        editTextPhone.removeTextChangedListener(phoneTextWatcher);
        editTextPhone.setText(text);
        int safeCursor = Math.min(text.length(), editTextPhone.getText().length());
        editTextPhone.setSelection(safeCursor);
        editTextPhone.addTextChangedListener(phoneTextWatcher);
    }

    private void updatePhoneHint() {
        String hint;
        if (selectedCountry != null && selectedCountry.isDefault()) {
            hint = getString(R.string.phone_hint);
        } else if ("+7".equals(selectedCountry != null ? selectedCountry.getCode() : null) &&
                "Казахстан".equals(selectedCountry != null ? selectedCountry.getName() : null)) {
            hint = getString(R.string.hint_kazakhstan);
        } else if ("+7".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_russia);
        } else if ("+375".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_belarus);
        } else if ("+374".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_armenia);
        } else if ("+972".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_israel);
        } else if ("+996".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_kyrgyzstan);
        } else if ("+998".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_uzbekistan);
        } else if ("+992".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_tajikistan);
        } else if ("+995".equals(selectedCountry != null ? selectedCountry.getCode() : null)) {
            hint = getString(R.string.hint_georgia);
        } else {
            hint = getString(R.string.phone_hint);
        }
        editTextPhone.setHint(hint);
    }

    private String formatRUInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(3, digits.length())));
            if (digits.length() > 3) {
                sb.append(") ").append(digits.substring(3, Math.min(6, digits.length())));
                if (digits.length() > 6) {
                    sb.append("-").append(digits.substring(6, Math.min(8, digits.length())));
                    if (digits.length() > 8) {
                        sb.append("-").append(digits.substring(8, Math.min(10, digits.length())));
                    }
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatKZInternal(String digits) {
        return formatRUInternal(digits);
    }

    private String formatBYInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(2, digits.length())));
            if (digits.length() > 2) {
                sb.append(") ").append(digits.substring(2, Math.min(5, digits.length())));
                if (digits.length() > 5) {
                    sb.append("-").append(digits.substring(5, Math.min(7, digits.length())));
                    if (digits.length() > 7) {
                        sb.append("-").append(digits.substring(7, Math.min(9, digits.length())));
                    }
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatAMInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(2, digits.length())));
            if (digits.length() > 2) {
                sb.append(") ").append(digits.substring(2, Math.min(5, digits.length())));
                if (digits.length() > 5) {
                    sb.append("-").append(digits.substring(5, Math.min(8, digits.length())));
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatILInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(1, digits.length())));
            if (digits.length() > 1) {
                sb.append(") ").append(digits.substring(1, Math.min(4, digits.length())));
                if (digits.length() > 4) {
                    sb.append("-").append(digits.substring(4, Math.min(8, digits.length())));
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatKGInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(3, digits.length())));
            if (digits.length() > 3) {
                sb.append(") ").append(digits.substring(3, Math.min(6, digits.length())));
                if (digits.length() > 6) {
                    sb.append("-").append(digits.substring(6, Math.min(9, digits.length())));
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatUZInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(2, digits.length())));
            if (digits.length() > 2) {
                sb.append(") ").append(digits.substring(2, Math.min(5, digits.length())));
                if (digits.length() > 5) {
                    sb.append("-").append(digits.substring(5, Math.min(7, digits.length())));
                    if (digits.length() > 7) {
                        sb.append("-").append(digits.substring(7, Math.min(9, digits.length())));
                    }
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatTJInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(2, digits.length())));
            if (digits.length() > 2) {
                sb.append(") ").append(digits.substring(2, Math.min(5, digits.length())));
                if (digits.length() > 5) {
                    sb.append("-").append(digits.substring(5, Math.min(7, digits.length())));
                    if (digits.length() > 7) {
                        sb.append("-").append(digits.substring(7, Math.min(9, digits.length())));
                    }
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private String formatGEInternal(String digits) {
        StringBuilder sb = new StringBuilder();
        if (digits.length() >= 1) {
            sb.append("(").append(digits.substring(0, Math.min(3, digits.length())));
            if (digits.length() > 3) {
                sb.append(") ").append(digits.substring(3, Math.min(6, digits.length())));
                if (digits.length() > 6) {
                    sb.append("-").append(digits.substring(6, Math.min(9, digits.length())));
                }
            }
        } else {
            sb.append(digits);
        }
        return sb.toString();
    }

    private int getMinDigitsForCountry(Country country) {
        if (country == null) return 6;
        switch (country.getCode()) {
            case "+7": return 10;
            case "+375": return 9;
            case "+374": return 8;
            case "+972":
            case "+996":
            case "+998":
            case "+992":
            case "+995": return 9;
            default: return 6;
        }
    }

    private int getMaxDigitsForCountry(Country country) {
        if (country == null) return 15;
        Integer val = maxDigitsMap.get(country.getCode());
        return (val != null) ? val : 15;
    }

    private boolean isValidPhoneNumber(String phone, Country country) {
        if (phone.isEmpty()) return false;
        String digits = phone.replaceAll("[^\\d]", "");
        int minDigits = getMinDigitsForCountry(country);
        int maxDigits = getMaxDigitsForCountry(country);
        return digits.length() >= minDigits && digits.length() <= maxDigits;
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", dayOfMonth, month + 1, year);
                    editTextDate.setText(formattedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    editTextTime.setText(formattedTime);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void createRequest() {
        String name = editTextClientName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String model = editTextModel.getText().toString().trim();
        String problem = editTextProblem.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.error_client_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (model.isEmpty()) {
            Toast.makeText(this, R.string.error_model, Toast.LENGTH_SHORT).show();
            return;
        }
        if (problem.isEmpty()) {
            Toast.makeText(this, R.string.error_problem, Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, R.string.error_date, Toast.LENGTH_SHORT).show();
            return;
        }
        if (time.isEmpty()) {
            Toast.makeText(this, R.string.error_time, Toast.LENGTH_SHORT).show();
            return;
        }

        // === ФИКС: автоопределение страны перед валидацией ===
        Country countryToValidate = selectedCountry;
        if (countryToValidate == null || countryToValidate.isDefault()) {
            countryToValidate = detectCountryByPhone(phone);
        }

        if (!isValidPhoneNumber(phone, countryToValidate)) {
            Toast.makeText(this, R.string.error_phone_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        long id = requestDao.createRequestWithDateTime(name, phone, model, problem, date + " " + time);
        if (id != -1L) {
            Toast.makeText(this, R.string.request_created, Toast.LENGTH_SHORT).show();
            sharedPrefsHelper.setCurrentScreen("main");
            finish();
        } else {
            Toast.makeText(this, R.string.request_creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRequestData(long id) {
        List<RequestItem> allRequests = requestDao.getAllRequests();
        RequestItem req = null;
        for (RequestItem item : allRequests) {
            if (item.getId() == id) {
                req = item;
                break;
            }
        }

        if (req != null) {
            editTextClientName.setText(req.getClient());
            editTextModel.setText(req.getModel());
            editTextProblem.setText(req.getProblem());

            String dt = req.getDateCreated();
            if (dt.contains(" ")) {
                String[] parts = dt.split(" ", 2);
                editTextDate.setText(parts[0]);
                editTextTime.setText(parts[1]);
            } else {
                editTextDate.setText(dt);
            }
            editTextDate.setEnabled(false);
            editTextTime.setEnabled(false);

            Country matchedCountry = null;
            String phone = req.getPhone();

            for (Country c : countries) {
                if (!c.isDefault() && phone.startsWith(c.getCode())) {
                    matchedCountry = c;
                    break;
                }
            }

            if (matchedCountry == null && phone.startsWith("+7")) {
                String digits = phone.substring(2).replaceAll("[^\\d]", "");
                if (digits.length() >= 3) {
                    String firstThree = digits.substring(0, 3);
                    if (firstThree.startsWith("6") || firstThree.startsWith("7") || firstThree.startsWith("8")) {
                        for (Country c : countries) {
                            if ("+7".equals(c.getCode()) && "Казахстан".equals(c.getName())) {
                                matchedCountry = c;
                                break;
                            }
                        }
                    }
                }
                if (matchedCountry == null) {
                    for (Country c : countries) {
                        if ("+7".equals(c.getCode()) && "Россия".equals(c.getName())) {
                            matchedCountry = c;
                            break;
                        }
                    }
                }
            }

            if (matchedCountry != null) {
                selectCountry(matchedCountry);
                safeSetPhoneText(phone);
            } else {
                resetToInitialState();
                safeSetPhoneText(phone);
            }
        } else {
            Toast.makeText(this, R.string.request_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateRequest() {
        if (currentRequestId == null) return;

        String name = editTextClientName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String model = editTextModel.getText().toString().trim();
        String problem = editTextProblem.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.error_client_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (model.isEmpty()) {
            Toast.makeText(this, R.string.error_model, Toast.LENGTH_SHORT).show();
            return;
        }
        if (problem.isEmpty()) {
            Toast.makeText(this, R.string.error_problem, Toast.LENGTH_SHORT).show();
            return;
        }

        // === ФИКС и здесь: определение страны перед валидацией ===
        Country countryToValidate = selectedCountry;
        if (countryToValidate == null || countryToValidate.isDefault()) {
            countryToValidate = detectCountryByPhone(phone);
        }

        if (!isValidPhoneNumber(phone, countryToValidate)) {
            Toast.makeText(this, R.string.error_phone_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsUpdated = requestDao.updateRequestData(currentRequestId, name, phone, model, problem);
        if (rowsUpdated > 0) {
            Toast.makeText(this, R.string.request_updated, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.request_update_error, Toast.LENGTH_SHORT).show();
        }
        sharedPrefsHelper.setCurrentScreen("main");
        finish();
    }
}