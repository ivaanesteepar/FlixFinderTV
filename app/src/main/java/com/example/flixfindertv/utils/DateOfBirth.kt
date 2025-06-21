package com.example.flixfindertv.utils

import android.app.DatePickerDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.Calendar

object DateOfBirth {

    @Composable
    fun EditableDatePicker(
        fecha: String,
        onFechaChange: (String) -> Unit,
        error: String?,
        onContinue: () -> Unit,
        setError: (String?) -> Unit
    ) {
        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    onFechaChange(selectedDate)
                    setError(null)
                },
                year, month, day
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = fecha,
                onValueChange = {
                    onFechaChange(it)
                    setError(null)
                },
                label = { Text("Birth Date (yyyy-mm-dd)") },
                isError = error != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                            contentDescription = "Select date"
                        )
                    }
                }
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                modifier = Modifier.fillMaxWidth(0.8f),
                onClick = {
                    if (fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                        onContinue()
                    } else {
                        setError("Please enter a valid date in format yyyy-mm-dd")
                    }
                }
            ) {
                Text("Continue")
            }
        }
    }
}