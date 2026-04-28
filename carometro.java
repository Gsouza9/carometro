package view;
 
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import model.DAO;
import utils.Validador;
 
public class Carometro extends JFrame {
	// instanciar objetos
	DAO dao = new DAO(); // importar
	private Connection con; // importar
	private PreparedStatement pst;
	private ResultSet rs;
	// instanciar objeto para o fluxo de bytes
	private FileInputStream fis;
	// váriavel global para armazenar o tamanho da imagem (bytes)
	private int tamanho;
	
	//variavel usada na correção do BUG
	private boolean fotoCarregada = false;
 
	private static final long serialVersionUID = 1L;
	private JLabel lblData;
	private JLabel lblStatus;
	private JLabel lblNewLabel;
	private JTextField txtRA;
	private JTextField txtNome;
	private JLabel lblFoto;
	private JPanel contentPane;
	private JButton btnCarregar;
	private JButton btnBuscar;
	private JScrollPane scrollPaneLista;
	private JList<String> listNomes;
	private JButton btnEditar;
	private JButton btnExcluir;
	private JButton btnReset;
	private JButton btnAdicionar;
	private JButton btnSobre;
	private JButton btnPdf;
 
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Carometro frame = new Carometro();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
 
	public Carometro() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				status();
				setarData();
			}
		});
		setTitle("Carômetro");
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Carometro.class.getResource("/img/instagram.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 645, 423);
		contentPane = new JPanel();
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.textHighlight);
		panel.setBounds(0, 332, 629, 52);
		contentPane.add(panel);
		panel.setLayout(null);
		lblStatus = new JLabel("New label");
		lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
		lblStatus.setBounds(587, 11, 32, 32);
		panel.add(lblStatus);
		lblData = new JLabel("");
		lblData.setForeground(SystemColor.text);
		lblData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblData.setBounds(10, 20, 193, 23);
		panel.add(lblData);
		lblNewLabel = new JLabel("RA");
		lblNewLabel.setBounds(23, 30, 46, 14);
		contentPane.add(lblNewLabel);
		txtRA = new JTextField();
		txtRA.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String caracteres = "0123456789";
				if (!caracteres.contains(e.getKeyChar() + "")) {
					e.consume();
				}
			}
		});
		txtRA.setBounds(52, 27, 86, 20);
		contentPane.add(txtRA);
		txtRA.setColumns(10);
		// uso do PlainDocument para limitar os campos
		txtRA.setDocument(new Validador(6));
		JLabel lblNewLabel_1 = new JLabel("Nome");
		lblNewLabel_1.setBounds(10, 51, 46, 14);
		contentPane.add(lblNewLabel_1);
		txtNome = new JTextField();
		txtNome.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				listarNomes();
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					scrollPaneLista.setVisible(false);
					int confirma = JOptionPane.showConfirmDialog(null, "Aluno(a) não cadastrado(a).\nDeseja cadastrar este aluno?","Aviso",JOptionPane.YES_OPTION);
					if (confirma == JOptionPane.YES_OPTION) {
						txtRA.setEditable(false);
						btnBuscar.setEnabled(false);
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
					}else {
						reset();
					}
				}
			}
		});
		txtNome.setBounds(52, 48, 180, 20);
		contentPane.add(txtNome);
		txtNome.setColumns(10);
		// uso do PlainDocument para limitar os campos
		txtNome.setDocument(new Validador(30));
		lblFoto = new JLabel("");
		lblFoto.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
		lblFoto.setBounds(341, 27, 256, 206);
		contentPane.add(lblFoto);
		btnCarregar = new JButton("Carregar Foto");
		btnCarregar.setEnabled(false);
		btnCarregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carregarFoto();
			}
		});
		btnCarregar.setForeground(SystemColor.textHighlight);
		btnCarregar.setBounds(122, 79, 110, 23);
		contentPane.add(btnCarregar);
		btnAdicionar = new JButton("");
		btnAdicionar.setEnabled(false);
		btnAdicionar.setBackground(new Color(153, 180, 209));
		btnAdicionar.setToolTipText("Adicionar");
		btnAdicionar.setIcon(new ImageIcon(Carometro.class.getResource("/img/create.png")));
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				adicionar();
			}
		});
		btnAdicionar.setBounds(10, 217, 73, 73);
		contentPane.add(btnAdicionar);
		btnReset = new JButton("");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		btnReset.setToolTipText("Limpar Campos");
		btnReset.setIcon(new ImageIcon(Carometro.class.getResource("/img/eraser.png")));
		btnReset.setBackground(UIManager.getColor("InternalFrame.activeTitleBackground"));
		btnReset.setBounds(259, 217, 73, 73);
		contentPane.add(btnReset);
		btnBuscar = new JButton("Buscar");
		btnBuscar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BuscarRA();
			}
		});
		btnBuscar.setBounds(146, 27, 86, 20);
		contentPane.add(btnBuscar);
		scrollPaneLista = new JScrollPane();
		scrollPaneLista.setBorder(null);
		scrollPaneLista.setVisible(false);
		scrollPaneLista.setFocusCycleRoot(true);
		scrollPaneLista.setBounds(53, 68, 179, 77);
		contentPane.add(scrollPaneLista);
		listNomes = new JList();
		scrollPaneLista.setViewportView(listNomes);
		
		btnEditar = new JButton("");
		btnEditar.setEnabled(false);
		btnEditar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editar();
			}
		});
		btnEditar.setToolTipText("Editar");
		btnEditar.setIcon(new ImageIcon(Carometro.class.getResource("/img/update.png")));
		btnEditar.setBackground(UIManager.getColor("InternalFrame.activeTitleBackground"));
		btnEditar.setBounds(93, 217, 73, 73);
		contentPane.add(btnEditar);
		
		btnExcluir = new JButton("");
		btnExcluir.setEnabled(false);
		btnExcluir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				excluir();
				
			}
		});
		btnExcluir.setToolTipText("Excluir");
		btnExcluir.setIcon(new ImageIcon(Carometro.class.getResource("/img/delete.png")));
		btnExcluir.setBackground(UIManager.getColor("InternalFrame.activeTitleBackground"));
		btnExcluir.setBounds(176, 217, 73, 73);
		contentPane.add(btnExcluir);
		
		JLabel lblNewLabel_2 = new JLabel("New label");
		lblNewLabel_2.setIcon(new ImageIcon(Carometro.class.getResource("/img/search.png")));
		lblNewLabel_2.setBounds(new Rectangle(24, 240, 0, 0));
		lblNewLabel_2.setBounds(242, 46, 24, 24);
		contentPane.add(lblNewLabel_2);
		
		btnSobre = new JButton("");
		btnSobre.setBorderPainted(false);
		btnSobre.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSobre.setContentAreaFilled(false);
		btnSobre.setIcon(new ImageIcon(Carometro.class.getResource("/img/info.png")));
		btnSobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Sobre sobre = new Sobre();
				sobre.setVisible(true);
				
			}
		});
		btnSobre.setBounds(571, 273, 48, 48);
		contentPane.add(btnSobre);
		
		
		btnPdf = new JButton("");
		btnPdf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gerarPdf();
			}
		});
		btnPdf.setIcon(new ImageIcon(Carometro.class.getResource("/img/pdf.png")));
		btnPdf.setToolTipText("Gerar lista de alunos");
		btnPdf.setBounds(381, 244, 64, 64);
		contentPane.add(btnPdf);
		listNomes.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buscarNome();
			}
		});
 
		// fim do construtor
		this.setLocationRelativeTo(null);
	}
	// criando o metodo de conexão com o banco de dados
 
	// testar a conexão com o banco de dados ↓↓
	private void status() {
		try {
			con = dao.conectar(); // con recebe dao
			// quando nao encontrar o banco ira aparecer isso
			if (con == null) {
				// System.out.println("Erro de Conexão");
				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
			} else { // quando conectar ira aparecer isso
				// System.out.println("Banco de dados Conectado");
				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dbon.png")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
 
	private void setarData() {
		Date data = new Date();
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL); // mostrar a data completa
		lblData.setText(formatador.format(data));
	}
 
	private void carregarFoto() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Selecionar Arquivo");
		jfc.setFileFilter(new FileNameExtensionFilter("Arquivo de imagens(*.PNG, *.JPEG, *.JPG", "png", "jpeg", "jpg"));
		int resultado = jfc.showOpenDialog(this);
		if (resultado == JFileChooser.APPROVE_OPTION) {
			try {
				fis = new FileInputStream(jfc.getSelectedFile());
				tamanho = (int) jfc.getSelectedFile().length();
				Image foto = ImageIO.read(jfc.getSelectedFile()).getScaledInstance(lblFoto.getWidth(),
						lblFoto.getHeight(), Image.SCALE_SMOOTH);
				lblFoto.setIcon(new ImageIcon(foto));
				lblFoto.updateUI();
				fotoCarregada=true;
			} catch (Exception e) {
				System.out.println(e);
			}
		}
 
	}
 
	private void adicionar() {
		if (txtNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Preencha o nome");
			txtNome.requestFocus();
		}else if (tamanho == 0) {
			JOptionPane.showMessageDialog(null, "Selecione uma foto do aluno(a)");
		} else {
			String insert = "insert into alunos (nome,foto) values(?,?)";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(insert);
				pst.setString(1, txtNome.getText());
				pst.setBlob(2, fis, tamanho);
				int confirma = pst.executeUpdate();
				if (confirma == 1) {
					JOptionPane.showMessageDialog(null, "Aluno(a) cadastrado com sucesso");
				} else {
					JOptionPane.showMessageDialog(null, "Erro! Aluno não cadastrado");
				}
				con.close();
 
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
 
	private void BuscarRA() {
		if (txtRA.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o RA");
		} else {
			String readRA = "select * from alunos where ra = ?";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(readRA);
				pst.setString(1, txtRA.getText());
				rs = pst.executeQuery();
				if (rs.next()) {
					txtNome.setText(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);
					byte[] img = blob.getBytes(1, (int) blob.length());
					BufferedImage imagem = null;
					try {
						imagem = ImageIO.read(new ByteArrayInputStream(img));
					} catch (Exception e) {
						System.out.println(e);
					}
					ImageIcon icone = new ImageIcon(imagem);
					Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblFoto.getWidth(),
							lblFoto.getHeight(), imagem.SCALE_SMOOTH));
					lblFoto.setIcon(foto);
					txtRA.setEditable(false);
					btnEditar.setEnabled(true);
					btnExcluir.setEnabled(true);
					btnCarregar.setEnabled(true);
					btnAdicionar.setEnabled(false);
					btnPdf.setEnabled(false);
					
				} else {
					int confirma = JOptionPane.showConfirmDialog(null, "Aluno(a) não cadastrado(a).\nDeseja iniciar um novo cadastro?","Aviso",JOptionPane.YES_OPTION);
					if (confirma == JOptionPane.YES_OPTION) {
						/*txtRA.setEditable(false);
						btnBuscar.setEnabled(false);
						txtNome.setText(null);
						txtNome.requestFocus();
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
						btnExcluir.setEnabled(true);*/
					}else {
						reset();
					}
				}
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
 
	private void listarNomes() {
 
		DefaultListModel<String> modelo = new DefaultListModel<>();
		listNomes.setModel(modelo);
 
		String readLista = "select * from alunos where nome like ? order by nome";
 
		try {
			con = dao.conectar();
			pst = con.prepareStatement(readLista);
			pst.setString(1, txtNome.getText() + "%");
 
			rs = pst.executeQuery();
 
			while (rs.next()) {
				scrollPaneLista.setVisible(true);
				modelo.addElement(rs.getString("nome"));
			}
 
			if (txtNome.getText().isEmpty()) {
				scrollPaneLista.setVisible(false);
			}
 
			con.close();
 
		} catch (Exception e) {
			System.out.println(e);
		}
	}
 
	private void buscarNome() {
 
		int linha = listNomes.getSelectedIndex();
 
		if (linha >= 0) {
			String readNome = "select * from alunos where nome like ? order by nome limit ?,1";
 
			try {
				con = dao.conectar(); 
				pst = con.prepareStatement(readNome);
				pst.setString(1, txtNome.getText() + "%");
				pst.setInt(2, linha);
				rs = pst.executeQuery();
 
				if (rs.next()) {
 					scrollPaneLista.setVisible(false);

					txtRA.setText(rs.getString("ra")); 
					txtNome.setText(rs.getString("nome"));
					Blob blob = rs.getBlob("foto"); 
					byte[] img = blob.getBytes(1, (int) blob.length()); 
					BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(img));
					ImageIcon icone = new ImageIcon(imagem);
					Icon foto = new ImageIcon(
 
							icone.getImage().getScaledInstance(lblFoto.getWidth(), lblFoto.getHeight(),	Image.SCALE_SMOOTH));
					lblFoto.setIcon(foto);
					txtRA.setEditable(false);
					btnBuscar.setEnabled(false);
					txtRA.setEditable(false);
					btnCarregar.setEnabled(true);
					btnAdicionar.setEnabled(true);
					btnExcluir.setEnabled(false);
					btnPdf.setEnabled(false);
				}
 
				con.close();
 
			} catch (Exception e) {
				System.out.println(e);
			}
 
		} else {
			scrollPaneLista.setVisible(false);
		}
 
	}
	
	private void editar() {
	    if (txtNome.getText().isEmpty()) {
	        JOptionPane.showMessageDialog(null, "Preencha o nome");
	        txtNome.requestFocus();
	        return;
	    }

	    try {
	        con = dao.conectar();

	        // 🔹 CASO TENHA CARREGADO NOVA FOTO
	        if (fotoCarregada) {
	            String update = "update alunos set nome=?, foto=? where ra=?";
	            pst = con.prepareStatement(update);
	            pst.setString(1, txtNome.getText());
	            pst.setBlob(2, fis, tamanho);
	            pst.setString(3, txtRA.getText());

	        // 🔹 CASO NÃO TENHA NOVA FOTO
	        } else {
	            String update = "update alunos set nome=? where ra=?";
	            pst = con.prepareStatement(update);
	            pst.setString(1, txtNome.getText());
	            pst.setString(2, txtRA.getText());
	        }

	        int confirma = pst.executeUpdate();

	        if (confirma == 1) {
	            JOptionPane.showMessageDialog(null, "Dados do aluno alterados com sucesso!");
	            reset();
	        } else {
	            JOptionPane.showMessageDialog(null, "Erro ao atualizar os dados!");
	        }

	        con.close();

	    } catch (Exception e) {
	        System.out.println(e);
	    }
	}
	private void excluir() {
		int confirmaExcluir = JOptionPane.showConfirmDialog(null, "Confirma a exclusão deste aluno?", "Atenção!", JOptionPane.YES_NO_OPTION);
		if(confirmaExcluir == JOptionPane.YES_OPTION) {
			String delete = "delete from alunos where ra=?";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(delete);
				pst.setString(1, txtRA.getText());
				int confirma = pst.executeUpdate();
				if (confirma == 1) {
					reset();
					JOptionPane.showMessageDialog(null, "Aluno deletado com sucesso!");
					
				}
				con.close();
				
			} catch (Exception e) {
				System.out.println(e);

			}
			
		}
	}
	private void gerarPdf() {
		Document document = new Document();
		//gerar o documento PDF
		try {
			PdfWriter.getInstance(document, new FileOutputStream("alunos.pdf"));
			document.open();
			Date data = new Date();
			DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);
			document.add(new Paragraph(formatador.format(data)));
			document.add(new Paragraph("Listagem de alunos:"));
			document.add(new Paragraph(" "));
			//tabela
			PdfPTable tabela = new PdfPTable(3);
			PdfPCell col1 = new PdfPCell(new Paragraph("RA"));
			tabela.addCell(col1);
			PdfPCell col2 = new PdfPCell(new Paragraph("NOME"));
			tabela.addCell(col2);
			PdfPCell col3 = new PdfPCell(new Paragraph("FOTO"));
			tabela.addCell(col3);
			String readLista = "select * from alunos order by nome";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(readLista);
				rs = pst.executeQuery();
				while(rs.next()) {
					tabela.addCell(rs.getString(1));
					tabela.addCell(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);
					byte[] img = blob.getBytes(1,(int)blob.length());
					com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(img);
					tabela.addCell(image);
				}
				con.close();
			} catch (Exception ex) {
				System.out.println(ex);
			}
			document.add(tabela);
			
		} catch (Exception e) {
			System.out.println(e);
			
		} finally {
			document.close();
		}
		//abrir o documento pdf no leitor padrão do sistema
		try {
			Desktop.getDesktop().open(new File("alunos.pdf"));
		} catch (Exception e2) {
			System.out.println(e2);
		}
	}
 
	private void reset() {
		txtRA.setText(null);
		txtNome.setText(null);
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
		txtNome.requestFocus();
		scrollPaneLista.setVisible(false);
		tamanho = 0;
		fotoCarregada = false; // 🔥 ESSENCIAL
		txtRA.setEditable(true);
		btnBuscar.setEnabled(true);
		btnCarregar.setEnabled(false);
		btnAdicionar.setEnabled(false);
		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
		btnPdf.setEnabled(true);
	}
}
 
