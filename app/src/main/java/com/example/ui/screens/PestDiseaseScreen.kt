package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DiseaseSeverity
import com.example.data.model.PestDiseaseDiagnosis
import com.example.data.model.SampleCropImage
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.PestDetectionUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PestDiseaseScreen(
  viewModel: AgriViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val pestState by viewModel.pestState.collectAsState()
  val pastScans by viewModel.pastPestScans.collectAsState()
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

  // Zero-permission Photo Picker launcher
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          val source = ImageDecoder.createSource(context.contentResolver, uri)
          ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
          }
        } else {
          @Suppress("DEPRECATION")
          MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        selectedBitmap = bitmap
        viewModel.analyzeCropImage(bitmap, uri.toString())
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Hero Card: AI Diagnostic Camera
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = null,
            tint = ActionGreen,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Crop Pest & Disease Detection",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Powered by Gemini 3.8 Flash Multimodal Pathology",
              style = MaterialTheme.typography.labelSmall,
              color = ActionGreen
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Upload or take a photo of discolored leaves, stem spots, or pest webs. The system identifies the pathogen, estimates severity, and provides immediate organic and chemical treatments.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Image Preview
        if (selectedBitmap != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
          ) {
            Image(
              bitmap = selectedBitmap!!.asImageBitmap(),
              contentDescription = "Analyzed leaf",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("upload_crop_photo_btn")
          ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Select Crop Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 2. Analysis Progress State
    if (pestState is PestDetectionUiState.Analyzing) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ActionGreen.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator(color = ActionGreen, strokeWidth = 3.dp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Analyzing foliage pathology with Gemini 3.8 Flash...",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Scanning for necrotic lesions, fungal spores, and insect stippling",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 3. Analysis Error State
    if (pestState is PestDetectionUiState.Error) {
      val errMsg = (pestState as PestDetectionUiState.Error).message
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatusCrit.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusCrit.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Analysis Notice",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = StatusCrit
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = errMsg,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Tip: You can also select a test card from the sample crop library below.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 4. Analysis Result Card
    if (pestState is PestDetectionUiState.Success) {
      val diag = (pestState as PestDetectionUiState.Success).diagnosis
      DiagnosisResultCard(diagnosis = diag)
    }

    // 5. Quick Test with Commercial Crop Samples
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Test with Commercial Crop Library",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Tap any field condition sample to inspect treatment protocols immediately:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        viewModel.sampleCrops.forEach { sample ->
          val sevColor = when (sample.sampleDiagnosis.severity) {
            DiseaseSeverity.CRITICAL -> StatusCrit
            DiseaseSeverity.HIGH -> StatusCrit
            DiseaseSeverity.MODERATE -> StatusWarn
            DiseaseSeverity.LOW -> StatusOk
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .clickable { viewModel.loadSampleDiagnosis(sample) }
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(sevColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(sample.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(sample.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Inspect", fontSize = 12.sp, color = ActionGreen, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }

    // 6. Past Diagnostic Scans History
    if (pastScans.isNotEmpty()) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Diagnostic Scan History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))

          pastScans.take(6).forEach { scan ->
            val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(scan.diagnosis, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${scan.cropName} · $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(
                    if (scan.severity == DiseaseSeverity.CRITICAL || scan.severity == DiseaseSeverity.HIGH) StatusCrit.copy(alpha = 0.15f)
                    else if (scan.severity == DiseaseSeverity.MODERATE) StatusWarn.copy(alpha = 0.15f)
                    else StatusOk.copy(alpha = 0.15f)
                  )
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = scan.severity.name,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (scan.severity == DiseaseSeverity.CRITICAL || scan.severity == DiseaseSeverity.HIGH) StatusCrit
                  else if (scan.severity == DiseaseSeverity.MODERATE) StatusWarn
                  else StatusOk
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun DiagnosisResultCard(
  diagnosis: PestDiseaseDiagnosis,
  modifier: Modifier = Modifier
) {
  val sevColor = when (diagnosis.severity) {
    DiseaseSeverity.CRITICAL -> StatusCrit
    DiseaseSeverity.HIGH -> StatusCrit
    DiseaseSeverity.MODERATE -> StatusWarn
    DiseaseSeverity.LOW -> StatusOk
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(2.dp, sevColor.copy(alpha = 0.8f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header Title & Severity Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = diagnosis.diagnosis,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${diagnosis.cropName} · ${diagnosis.affectedPart}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(sevColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = "${diagnosis.severity.name} SEVERITY",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Pathology Confidence: ${(diagnosis.confidenceScore * 100).toInt()}%",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = ActionGreen
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Symptoms Observed
      Text(
        text = "Observed Symptoms:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      diagnosis.symptomsObserved.forEach { symptom ->
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
          Text("• ", fontWeight = FontWeight.Bold, color = sevColor)
          Text(symptom, style = MaterialTheme.typography.bodySmall)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Organic Treatments
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(StatusOk.copy(alpha = 0.08f))
          .border(1.dp, StatusOk.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
          .padding(12.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = StatusOk, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Organic & Biological Treatments",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = StatusOk
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          diagnosis.organicTreatments.forEach { trt ->
            Text("✓ $trt", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Chemical Treatments
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
          .padding(12.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF6A47B5), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Chemical & Fungicide Options",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF6A47B5)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          diagnosis.chemicalTreatments.forEach { chem ->
            Text("• $chem", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Preventative Cultural Practices
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(12.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Healing, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Agronomic Prevention Protocol",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          diagnosis.preventativeMeasures.forEach { prev ->
            Text("→ $prev", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
          }
        }
      }
    }
  }
}
