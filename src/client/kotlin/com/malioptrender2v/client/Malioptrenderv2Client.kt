package com.malioptrender2v.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.LoggerFactory

object Malioptrenderv2Client : ClientModInitializer {
    private val LOGGER = LoggerFactory.getLogger("MaliOpt")

    override fun onInitializeClient() {
        LOGGER.info("MaliOpt V2: Aguardando janela para ler hardware...")

        ClientLifecycleEvents.CLIENT_STARTED.register { client ->
            LOGGER.info("[MaliOpt] Janela aberta. Iniciando scanner de GPU...")
            MaliHardware.checkExtensions()
        }
    }
}
