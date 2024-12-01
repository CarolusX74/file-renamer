import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.awt.dnd.DropTarget; //- Importar DropTarget
import java.awt.dnd.DnDConstants; //- Importar DnDConstants
import java.awt.dnd.DropTargetAdapter; //- Importar DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent; //- Importar DropTargetDropEvent

//- File Renamer DLV (De La Vida)
public class FileRenamerApp {

    private JFrame frame;
    private JTextField txtSuffixA;
    private JLabel fileCounterLabel; //- Etiqueta para mostrar el contador
    private DefaultListModel<FileWithSuffix> fileListModel;
    private File destinationFolder;

    static class FileWithSuffix {
        File file;
        String suffixB;

        public FileWithSuffix(File file, String suffixB) {
            this.file = file;
            this.suffixB = suffixB;
        }

        @Override
        public String toString() {
            return file.getName() + " (" + suffixB + ")";
        }
    }

    public FileRenamerApp() {
        frame = new JFrame("File Renamer by CarolusX74");
        frame.setSize(600, 500);  //- Aumentar el tamaño para dejar espacio para la sección de donación
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Sufijo A:"), BorderLayout.WEST);
        txtSuffixA = new JTextField();
        inputPanel.add(txtSuffixA, BorderLayout.CENTER);

        //- Mover el contador fileCounterLabel debajo de "Sufijo A"
        JPanel suffixPanel = new JPanel(new BorderLayout());
        suffixPanel.add(inputPanel, BorderLayout.NORTH);

        fileCounterLabel = new JLabel("Archivos cargados: 0");
        fileCounterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        suffixPanel.add(fileCounterLabel, BorderLayout.SOUTH);

        mainPanel.add(suffixPanel, BorderLayout.NORTH);

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS)); //- Usar BoxLayout para los botones

        fileListModel = new DefaultListModel<>();
        JList<FileWithSuffix> fileList = new JList<>(fileListModel);
        JScrollPane fileScrollPane = new JScrollPane(fileList);

        //- Habilitar Drag & Drop en el JList
        new DropTarget(fileList, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Object transferData = event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (transferData instanceof List) {
                        List<?> rawList = (List<?>) transferData;
                        List<File> droppedFiles = rawList.stream()
                                                         .filter(item -> item instanceof File)
                                                         .map(item -> (File) item)
                                                         .toList();
                        addFiles(droppedFiles.toArray(new File[0]));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        filePanel.add(fileScrollPane);

        JPanel fileButtonPanel = new JPanel();
        JButton btnAddFiles = new JButton("Agregar Archivos");
        btnAddFiles.addActionListener(e -> openFileChooser());

        JButton btnRemoveFiles = new JButton("Eliminar Seleccionados");
        btnRemoveFiles.addActionListener(e -> {
            fileList.getSelectedValuesList().forEach(fileListModel::removeElement);
            updateFileCounter(); //- Actualizar contador después de eliminar archivos
        });

        fileButtonPanel.add(btnAddFiles);
        fileButtonPanel.add(btnRemoveFiles);

        //- Añadir los botones en el orden correcto
        filePanel.add(fileButtonPanel);

        //- Mover el botón de renombrado debajo de los botones "Agregar Archivos"
        JPanel renameButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRenameFiles = new JButton("Renombrar y Copiar Archivos");
        btnRenameFiles.addActionListener(e -> renameAndCopyFiles());
        renameButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //- Espaciado extra
        renameButtonPanel.add(btnRenameFiles);

        filePanel.add(renameButtonPanel);

        mainPanel.add(filePanel, BorderLayout.CENTER);

        //- Panel para la opción de Donar
        JPanel donatePanel = new JPanel();
        donatePanel.setLayout(new BoxLayout(donatePanel, BoxLayout.Y_AXIS));
        donatePanel.setBorder(BorderFactory.createTitledBorder("Donar"));

        //- Agregar el título y texto de donación en Binance
        JPanel binancePanel = new JPanel(new BorderLayout());
        JLabel binanceLabel = new JLabel("Binance:");
        JTextArea binanceText = new JTextArea("0x1b6ce0b673f1b8fef45443b41c4c9b30c5a908ff");
        binanceText.setEditable(false);
        binanceText.setBackground(Color.WHITE);  //- Usar un color de fondo explícito
        binanceText.setWrapStyleWord(true);
        binanceText.setLineWrap(true);
        binancePanel.add(binanceLabel, BorderLayout.WEST);
        binancePanel.add(new JScrollPane(binanceText), BorderLayout.CENTER);
        donatePanel.add(binancePanel);

        //- Agregar el título y texto de donación en Pesos Argentinos
        JPanel pesosPanel = new JPanel(new BorderLayout());
        JLabel pesosLabel = new JLabel("Pesos argentinos:");
        JTextArea pesosText = new JTextArea("PENSA.BRUBANK.AARS");
        pesosText.setEditable(false);
        pesosText.setBackground(Color.WHITE);  //- Usar un color de fondo explícito
        pesosText.setWrapStyleWord(true);
        pesosText.setLineWrap(true);
        pesosPanel.add(pesosLabel, BorderLayout.WEST);
        pesosPanel.add(new JScrollPane(pesosText), BorderLayout.CENTER);
        donatePanel.add(pesosPanel);

        //- Hacer que el contenido de los JTextArea se copie al portapapeles cuando se haga clic
        addCopyOnClickListener(binanceText);
        addCopyOnClickListener(pesosText);

        //- Agregar el panel de donaciones al panel principal
        mainPanel.add(donatePanel, BorderLayout.PAGE_END);

        frame.add(mainPanel);
    }

    private void addCopyOnClickListener(JTextArea textArea) {
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = textArea.getText();
                StringSelection stringSelection = new StringSelection(text);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(frame, "El contenido ha sido copiado al portapapeles.", "Copiado", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            addFiles(fileChooser.getSelectedFiles());
        }
    }

    private void addFiles(File[] files) {
        String[] suffixes = {
                "EDESUR", "METROGAS", "AySA", "ABL",
                "EXPENSAS RESUMEN", "EXPENSAS CUPON",
                "EXPENSAS RECIBO", "EXPENSAS PAGO", "OTRO"
        };

        for (File file : files) {
            String suffixB = (String) JOptionPane.showInputDialog(
                    frame,
                    "Selecciona el Sufijo B para: " + file.getName(),
                    "Sufijo B",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    suffixes,
                    suffixes[0]
            );

            if (suffixB != null) {
                if ("OTRO".equals(suffixB)) {
                    suffixB = JOptionPane.showInputDialog(
                            frame,
                            "Introduce el nombre del Sufijo B personalizado:",
                            "Sufijo B Personalizado",
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (suffixB == null || suffixB.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Sufijo B personalizado no válido. Se omite este archivo.");
                        continue;
                    }
                }

                FileWithSuffix fileWithSuffix = new FileWithSuffix(file, suffixB);
                fileListModel.addElement(fileWithSuffix);
            }
        }
        updateFileCounter(); //- Actualizar contador al agregar archivos
    }

    private void renameAndCopyFiles() {
        String suffixA = txtSuffixA.getText().trim();

        if (suffixA.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Por favor, introduce un sufijo A.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fileListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Por favor, agrega archivos a la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            destinationFolder = new File(suffixA);
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }

            for (int i = 0; i < fileListModel.size(); i++) {
                FileWithSuffix fileWithSuffix = fileListModel.getElementAt(i);
                File originalFile = fileWithSuffix.file;
                String suffixB = fileWithSuffix.suffixB;

                String newFileName = suffixA + " - " + suffixB + " - " + originalFile.getName();
                File newFile = new File(destinationFolder, newFileName);

                Files.copy(originalFile.toPath(), newFile.toPath());
            }

            JOptionPane.showMessageDialog(frame, "Archivos copiados y renombrados correctamente en la carpeta: " + suffixA);
            fileListModel.clear();
            updateFileCounter(); //- Actualizar el contador después de renombrar los archivos
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error al copiar y renombrar archivos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFileCounter() {
        fileCounterLabel.setText("Archivos cargados: " + fileListModel.getSize());
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileRenamerApp().show());
    }
}
