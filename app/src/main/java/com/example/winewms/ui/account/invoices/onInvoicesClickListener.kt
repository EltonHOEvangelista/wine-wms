package com.example.winewms.ui.account.invoices

import com.example.winewms.data.model.WineInvoice

interface onInvoicesClickListener {
    fun onInvoicesClickListener(wineInvoice: WineInvoice)
}