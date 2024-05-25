package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TipTimeLayout()
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout() {
    var amountInput by remember { mutableStateOf("") }
    var tipPercentage by remember { mutableDoubleStateOf(10.0) }
    var roundUp by remember { mutableStateOf(false) }
    var peopleInput by remember { mutableStateOf("1") }
    var showSplitDialog by remember { mutableStateOf(false) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val people = peopleInput.toIntOrNull() ?: 1
    val tipAmount = calculateTip(amount, tipPercentage, roundUp)
    val totalAmount = if (roundUp) kotlin.math.ceil(amount + tipAmount) else amount + tipAmount
    val amountPerPerson = totalAmount / people
    val currentDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())

    if (showSplitDialog) {
        SplitBillDialog(
            onDismiss = { showSplitDialog = false },
            onSplit = { peopleInput = it; showSplitDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NavigationBar(modifier = Modifier.padding(bottom = 16.dp))
        EditNumberField(
            label = R.string.bill_amount,
            leadingIcon = R.drawable.money,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = amountInput,
            onValueChanged = { amountInput = it },
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
        )
        Button(
            onClick = { showSplitDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = stringResource(R.string.split_bill), color = Color.White)
        }
        TipPercentageSelector(
            tipPercentage = tipPercentage,
            onTipPercentageChanged = { tipPercentage = it },
            modifier = Modifier.padding(bottom = 32.dp)
        )
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it },
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Divider(thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        BillSummary(
            tipAmount = tipAmount,
            totalAmount = totalAmount,
            amountPerPerson = amountPerPerson,
            people = people,
            roundUp = roundUp,
            currentDate = currentDate,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.tip), // Replace with your logo drawable resource
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
fun SplitBillDialog(
    onDismiss: () -> Unit,
    onSplit: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSplit(input) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column {
                Text("Enter number of people:")
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun EditNumberField(
    @StringRes label: Int,
    @DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        singleLine = true,
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) },
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun TipPercentageSelector(
    tipPercentage: Double,
    onTipPercentageChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(5.0, 10.0, 15.0, 20.0).forEach { percentage ->
            Button(
                onClick = { onTipPercentageChanged(percentage) },
                colors = if (percentage == tipPercentage) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                } else {
                    ButtonDefaults.buttonColors(containerColor = Color.Gray)
                },
                modifier = Modifier
                    .padding(0.dp)
                    .width(75.dp)
                    .height(55.dp)
            ) {
                Text(text = "${percentage.toInt()}%")
            }
        }
    }
}

@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.round_up_tip))
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

@Composable
fun BillSummary(
    tipAmount: Double,
    totalAmount: Double,
    amountPerPerson: Double,
    people: Int,
    roundUp: Boolean,
    currentDate: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Your Bill Summary",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = currentDate,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Divider(thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        SummaryLineItem(label = "Bill Amount", amount = totalAmount - tipAmount)
        SummaryLineItem(label = "Tip", amount = tipAmount)
        SummaryLineItem(label = "Amount Per Person", amount = amountPerPerson)
        SummaryLineItem(label = "Number of People", amount = people.toDouble(), isCurrency = false)
        SummaryLineItem(label = "Total Amount", amount = totalAmount)
        Divider(thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        if (roundUp) {
            Text(
                text = "Thank you for the round up!",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Divider(thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun SummaryLineItem(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
    isCurrency: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp)
        Text(
            text = if (isCurrency) NumberFormat.getCurrencyInstance().format(amount) else amount.toInt().toString(),
            fontSize = 18.sp,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */
private fun calculateTip(amount: Double, tipPercent: Double, roundUp: Boolean): Double {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }
    return tip
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}
