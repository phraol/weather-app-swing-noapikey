import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherAppSwingNoAPIKey {
    private static final String BASE_URL = "http://wttr.in/";
    private static final HttpClient client = HttpClient.newHttpClient();

    private JFrame frame;
    private JTextField cityField;
    private JComboBox<String> unitCombo;
    private JTextArea weatherDisplay;

    public WeatherAppSwingNoAPIKey() {
        // Set up the main window
        frame = new JFrame("Weather App (No API Key)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        cityField = new JTextField(15);
        cityField.setToolTipText("Enter city name (e.g., London)");
        JButton fetchButton = new JButton("Get Weather");
        unitCombo = new JComboBox<>(new String[]{"Celsius", "Fahrenheit"});

        inputPanel.add(new JLabel("City:"));
        inputPanel.add(cityField);
        inputPanel.add(new JLabel("Units:"));
        inputPanel.add(unitCombo);
        inputPanel.add(fetchButton);

        // Weather display area
        weatherDisplay = new JTextArea(10, 30);
        weatherDisplay.setEditable(false);
        weatherDisplay.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(weatherDisplay);

        // Add components to frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Button and Enter key action
        fetchButton.addActionListener(this::fetchWeatherAction);
        cityField.addActionListener(this::fetchWeatherAction);

        frame.setVisible(true);
    }

    // Handle fetch weather action
    private void fetchWeatherAction(ActionEvent e) {
        String city = cityField.getText().trim().replace(" ", "_"); // wttr.in uses underscores
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a city!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String units = unitCombo.getSelectedItem().equals("Celsius") ? "m" : "u"; // m=metric, u=imperial
        try {
            String weatherData = fetchWeather(city, units);
            displayWeather(weatherData, units);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Fetch Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Fetch weather data from wttr.in
    private String fetchWeather(String city, String units) throws Exception {
        String url = BASE_URL + city + "?format=%t+%h+%w+%C&" + units; // Custom format: temp, humidity, wind, condition
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 || response.body().contains("ERROR")) {
            throw new Exception("Could not fetch weather for " + city + ". Try a different city.");
        }

        return response.body();
    }

    // Display weather data (parse wttr.in's custom response)
    private void displayWeather(String weatherData, String units) {
        String[] parts = weatherData.split("\\s+"); // Split by whitespace
        if (parts.length < 4) {
            weatherDisplay.setText("Error parsing weather data.");
            return;
        }

        String temp = parts[0]; // e.g., "+23°C" or "73°F"
        String humidity = parts[1].replace("%", ""); // e.g., "65%"
        String wind = parts[2]; // e.g., "10km/h" or "6mph"
        String condition = String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length)); // Rest is condition

        String unitSymbol = units.equals("m") ? "°C" : "°F";
        String windUnit = units.equals("m") ? "km/h" : "mph";

        weatherDisplay.setText(String.format(
                "Weather in %s:\n" +
                        "Temperature: %s\n" +
                        "Humidity: %s%%\n" +
                        "Wind Speed: %s %s\n" +
                        "Condition: %s",
                cityField.getText(), temp, humidity, wind, windUnit, condition
        ));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherAppSwingNoAPIKey::new);
    }
}
