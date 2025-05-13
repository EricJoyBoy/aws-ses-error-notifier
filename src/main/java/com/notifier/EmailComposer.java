package com.notifier;

import java.util.Map;

public final class EmailComposer {
    private static final String TABLE_STYLE = "border: 1px solid black; vertical-align: top";
    private static final String HTML_TEMPLATE = """
            <style>
                table { border-collapse: collapse; margin: 10px 0; }
                td, th { %s; padding: 8px; }
                th { text-align: left; background-color: #f2f2f2; }
            </style>
            <h3>Errore durante la notifica del seguente messaggio:</h3>
            <table>
                <thead><tr><th>Field</th><th>Value</th></tr></thead>
                <tbody>%s</tbody>
            </table>
            <p>Please investigate this processing error.</p>
            """;

    public static String createEmailContent(Map<String, Object> record) {
        StringBuilder rows = new StringBuilder();
        addRow(rows, "Message ID", record.get("messageId"));
        addRow(rows, "Body", record.get("body"));
        addRow(rows, "Attributes", record.get("attributes"));
        addRow(rows, "Message Attributes", record.get("messageAttributes"));

        return String.format(HTML_TEMPLATE, TABLE_STYLE, rows);
    }

    private static void addRow(StringBuilder sb, String field, Object value) {
        sb.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td><pre>%s</pre></td>
                </tr>
                """, escapeHtml(field), escapeHtml(value)));
    }

    private static String escapeHtml(Object content) {
        if (content == null) return "N/A";
        return content.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}