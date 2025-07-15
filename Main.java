import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Patient {
    int id;
    String name;
    int age;
    String gender;
    String disease;
    String description;

    public Patient(int id, String name, int age, String gender, String disease, String description) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.disease = disease;
        this.description = description;
    }

    public String toString() {
        return "ID: " + id +
               "\nName: " + name +
               "\nAge: " + age +
               "\nGender: " + gender +
               "\nDisease: " + disease +
               "\nDescription: " + description +
               "\n------------------------\n";
    }

    public String toFileString() {
        return id + "|" + name + "|" + age + "|" + gender + "|" + disease + "|" + description.replace("\n", "\\n");
    }

    public static Patient fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 6) {
            return new Patient(
                Integer.parseInt(parts[0]),
                parts[1],
                Integer.parseInt(parts[2]),
                parts[3],
                parts[4],
                parts[5].replace("\\n", "\n")
            );
        }
        return null;
    }
}

// Custom JPanel to draw background image
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            backgroundImage = Toolkit.getDefaultToolkit().createImage(imagePath);
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(backgroundImage, 0);
            tracker.waitForAll();
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e);
        }
        setLayout(new BorderLayout(20, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

public class Main extends JFrame implements ActionListener {
    static ArrayList<Patient> patients = new ArrayList<>();
    JButton addBtn, viewBtn, searchBtn, deleteBtn, updateBtn, saveBtn, loadBtn, exitBtn;

    public Main() {
        setTitle("Hospital Management System");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set background panel with image
        BackgroundPanel backgroundPanel = new BackgroundPanel("background.jpg");
        setContentPane(backgroundPanel);

        JLabel title = new JLabel("Hospital Management System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        backgroundPanel.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // make buttons transparent over background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        addBtn = new JButton("➕ Add Patient");
        viewBtn = new JButton("📋 View All Patients");
        searchBtn = new JButton("🔍 Search Patient");
        deleteBtn = new JButton("❌ Delete by ID");
        updateBtn = new JButton("✏️ Update Patient");
        saveBtn = new JButton("💾 Save to File");
        loadBtn = new JButton("📂 Load from File");
        exitBtn = new JButton("🚪 Exit");

        JButton[] buttons = {addBtn, viewBtn, searchBtn, deleteBtn, updateBtn, saveBtn, loadBtn, exitBtn};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setFont(new Font("SansSerif", Font.PLAIN, 16));
            buttons[i].setBackground(new Color(255, 255, 255, 230)); // semi-transparent
            buttons[i].setFocusPainted(false);
            buttons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            gbc.gridy = i;
            panel.add(buttons[i], gbc);
            buttons[i].addActionListener(this);
        }

        backgroundPanel.add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == addBtn) addPatientGUI();
        else if (src == viewBtn) viewPatientsGUI();
        else if (src == searchBtn) searchPatientGUI();
        else if (src == deleteBtn) deletePatientGUI();
        else if (src == updateBtn) updatePatientGUI();
        else if (src == saveBtn) saveToFile();
        else if (src == loadBtn) loadFromFile();
        else if (src == exitBtn) System.exit(0);
    }

    private void addPatientGUI() {
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField idF = new JTextField();
        JTextField nameF = new JTextField();
        JTextField ageF = new JTextField();
        JComboBox<String> genderF = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField diseaseF = new JTextField();
        JTextArea descF = new JTextArea(3, 20);
        form.add(new JLabel("Patient ID:")); form.add(idF);
        form.add(new JLabel("Name:")); form.add(nameF);
        form.add(new JLabel("Age:")); form.add(ageF);
        form.add(new JLabel("Gender:")); form.add(genderF);
        form.add(new JLabel("Disease:")); form.add(diseaseF);
        form.add(new JLabel("Description:")); form.add(new JScrollPane(descF));

        if (JOptionPane.showConfirmDialog(this, form, "Add New Patient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                patients.add(new Patient(
                    Integer.parseInt(idF.getText()),
                    nameF.getText(),
                    Integer.parseInt(ageF.getText()),
                    genderF.getSelectedItem().toString(),
                    diseaseF.getText(),
                    descF.getText()
                ));
                JOptionPane.showMessageDialog(this, "✅ Patient added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid input!");
            }
        }
    }

    private void viewPatientsGUI() {
        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patients to display.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Patient p : patients) sb.append(p.toString());
        JTextArea txt = new JTextArea(sb.toString());
        txt.setEditable(false);
        JScrollPane sp = new JScrollPane(txt);
        sp.setPreferredSize(new Dimension(500, 350));
        JOptionPane.showMessageDialog(this, sp, "Patient Records", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deletePatientGUI() {
        try {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Patient ID to delete:"));
            Iterator<Patient> it = patients.iterator();
            while (it.hasNext()) {
                if (it.next().id == id) {
                    it.remove();
                    JOptionPane.showMessageDialog(this, "✅ Deleted.");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Patient not found.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }

    private void updatePatientGUI() {
        try {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Patient ID to update:"));
            for (Patient p : patients) {
                if (p.id == id) {
                    JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
                    JTextField nameF = new JTextField(p.name);
                    JTextField ageF = new JTextField(String.valueOf(p.age));
                    JComboBox<String> genderF = new JComboBox<>(new String[]{"Male", "Female", "Other"});
                    genderF.setSelectedItem(p.gender);
                    JTextField diseaseF = new JTextField(p.disease);
                    JTextArea descF = new JTextArea(p.description, 3, 20);

                    form.add(new JLabel("Name:")); form.add(nameF);
                    form.add(new JLabel("Age:")); form.add(ageF);
                    form.add(new JLabel("Gender:")); form.add(genderF);
                    form.add(new JLabel("Disease:")); form.add(diseaseF);
                    form.add(new JLabel("Description:")); form.add(new JScrollPane(descF));

                    if (JOptionPane.showConfirmDialog(this, form, "Update Patient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        p.name = nameF.getText();
                        p.age = Integer.parseInt(ageF.getText());
                        p.gender = genderF.getSelectedItem().toString();
                        p.disease = diseaseF.getText();
                        p.description = descF.getText();
                        JOptionPane.showMessageDialog(this, "✅ Updated.");
                    }
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Patient not found.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating.");
        }
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter("patients.txt")) {
            for (Patient p : patients) {
                pw.println(p.toFileString());
            }
            JOptionPane.showMessageDialog(this, "✅ Saved to patients.txt");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error saving file.");
        }
    }

    private void loadFromFile() {
        File file = new File("patients.txt");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "No saved file found.");
            return;
        }

        try (Scanner sc = new Scanner(file)) {
            patients.clear();
            while (sc.hasNextLine()) {
                Patient p = Patient.fromFileString(sc.nextLine());
                if (p != null) patients.add(p);
            }
            JOptionPane.showMessageDialog(this, "✅ Loaded from patients.txt");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading file.");
        }
    }

    private void searchPatientGUI() {
        String[] options = {"Search by ID", "Search by Name", "Search by Disease"};
        JComboBox<String> searchOptions = new JComboBox<>(options);
        JTextField inputField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Search Type:"));
        panel.add(searchOptions);
        panel.add(new JLabel("Search Value:"));
        panel.add(inputField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Search Patient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String query = inputField.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a search value.");
                return;
            }
            String type = (String) searchOptions.getSelectedItem();
            for (Patient p : patients) {
                if ((type.equals("Search by ID") && String.valueOf(p.id).equals(query)) ||
                    (type.equals("Search by Name") && p.name.equalsIgnoreCase(query)) ||
                    (type.equals("Search by Disease") && p.disease.equalsIgnoreCase(query))) {
                    JOptionPane.showMessageDialog(this, p.toString());
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Patient not found.");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        new Main();
    }
}
