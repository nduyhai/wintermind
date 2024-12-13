package com.github.nduyhai.wintermind.toolWindow

import com.github.nduyhai.wintermind.services.WinterMindProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.ToolWindow
import java.awt.BorderLayout
import javax.swing.*

class WinterMindWindow(toolWindow: ToolWindow) {
    private val service = toolWindow.project.service<WinterMindProjectService>()
    private val textArea = JTextArea()
    private val inputField = JTextField()
    private val sendButton = JButton("Send")

    fun getContent(): JPanel {
        val panel = JPanel(BorderLayout())

        textArea.isEditable = false
        panel.add(JScrollPane(textArea), BorderLayout.CENTER)

        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(sendButton, BorderLayout.EAST)
        panel.add(inputPanel, BorderLayout.SOUTH)

        sendButton.addActionListener {
            val prompt = inputField.text
            if (prompt.isNotEmpty()) {
                val response = service.prompt(prompt)
                textArea.append("You: $prompt\n")
                textArea.append("AI: $response\n\n")
                inputField.text = ""
            }
        }

        return panel;
    }
}