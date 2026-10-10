package dev.PC22200322y22200350.pc1.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF17483D)
val DeepForest = Color(0xFF123D35)
val Lime = Color(0xFFD8F36A)
val PaleGreen = Color(0xFFF0F4EE)
val AppBackground = Color(0xFFF7F8F3)
val MutedText = Color(0xFF78877E)
val SuccessGreen = Color(0xFF387448)

@Composable
fun AppPage(content: @Composable () -> Unit) {
    Surface(color = AppBackground, contentColor = DeepForest) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
        ) { content() }
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(42.dp).background(Lime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✦", color = DeepForest, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text("BOLILLA", fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text("LOTERÍA EN VIVO", color = MutedText, fontSize = 10.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.weight(1f))
        Surface(color = PaleGreen, shape = RoundedCornerShape(50.dp)) {
            Text(
                text = "●  EN VIVO",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                color = SuccessGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), modifier = modifier, color = Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineMedium, color = DeepForest, fontWeight = FontWeight.Bold)
    if (subtitle != null) {
        Spacer(Modifier.height(7.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedText)
    }
}

@Composable
fun NumberBall(
    number: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bg = if (selected) Lime else Color.White
    val fg = DeepForest
    Surface(
        modifier = modifier.size(46.dp),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = CircleShape,
        color = bg,
        contentColor = fg,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5DB))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(number.toString().padStart(2, '0'), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NumberGrid(
    numbers: List<Int>,
    modifier: Modifier = Modifier,
    highlightNumbers: Set<Int> = emptySet(),
    columns: Int = 6
) {
    val rows = numbers.chunked(columns)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                row.forEach { number ->
                    NumberBall(number, selected = number in highlightNumbers)
                }
            }
        }
    }
}

@Composable
fun LoadingMessage(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Forest)
        Spacer(Modifier.height(16.dp))
        Text(text, color = MutedText, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatusCard(title: String, message: String, icon: String = "i", tone: Color = PaleGreen) {
    Surface(color = tone, shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(tone).then(Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Surface(color = Color.White.copy(alpha = .65f), shape = CircleShape, modifier = Modifier.size(30.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(icon, color = Forest, fontWeight = FontWeight.Bold) }
                }
            }
            Column(Modifier.weight(1f).then(Modifier)) {
                Text(title, fontWeight = FontWeight.Bold, color = DeepForest)
                Spacer(Modifier.height(3.dp))
                Text(message, color = MutedText, fontSize = 12.sp)
            }
            Spacer(Modifier.width(14.dp))
        }
    }
}
