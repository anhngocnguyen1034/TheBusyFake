package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel

/**
 * Returns the localized display name for a contact.
 * Preset contacts (Mom, Partner, Doctor, Scientist) use string resources; others return as-is.
 */
@Composable
fun getContactDisplayName(contactName: String): String = when (contactName) {
    MessageViewModel.PRESET_MOM, "Mẹ" -> stringResource(R.string.preset_contact_mom)
    MessageViewModel.PRESET_LOVER, "Người yêu" -> stringResource(R.string.preset_contact_lover)
    MessageViewModel.PRESET_DOCTOR, "Bác sĩ" -> stringResource(R.string.preset_contact_doctor)
    MessageViewModel.PRESET_SCIENTIST, "Nhà khoa học" -> stringResource(R.string.preset_contact_scientist)
    else -> contactName
}
