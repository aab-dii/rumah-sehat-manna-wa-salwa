package com.android.rumahsehatmannawasalwa.ui.components.inputs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.android.rumahsehatmannawasalwa.ui.theme.*

@Composable
fun MannaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    maxLength: Int? = null,
    colors: TextFieldColors? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    focusRequester: FocusRequester? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else SlateText
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            placeholder = { Text(placeholder, color = GrayText) },
            isError = isError,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isError) MaterialTheme.colorScheme.error else BodyGray
                    )
                }
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            readOnly = readOnly,
            enabled = enabled,
            colors = colors ?: OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenLight,
                unfocusedBorderColor = DividerColor,
                cursorColor = GreenLight,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorLeadingIconColor = MaterialTheme.colorScheme.error,
                selectionColors = TextSelectionColors(
                    handleColor = GreenLight,
                    backgroundColor = GreenLight.copy(alpha = 0.4f)
                )
            ),
            shape = RoundedCornerShape(25.dp),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        // Area teks pendukung: error message, helper text, dan counter karakter
        val hasError = isError && !errorMessage.isNullOrEmpty()
        val hasSupporting = supportingText != null
        val hasCounter = maxLength != null
        val isOverLimit = maxLength != null && value.length > maxLength

        if (hasError || hasSupporting || hasCounter) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Kolom kiri: error message atau supporting text
                if (hasError) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else if (hasSupporting) {
                    Text(
                        text = supportingText!!,
                        color = BodyGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Kolom kanan: counter karakter
                if (hasCounter) {
                    Text(
                        text = "${value.length}/$maxLength",
                        color = if (isOverLimit) MaterialTheme.colorScheme.error else BodyGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
