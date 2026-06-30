import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Painel raiz da aplica\u00e7\u00e3o: monta o cabe\u00e7alho, a cena 3D, a descri\u00e7\u00e3o
 * do problema, a barra de controles e a barra lateral.
 *
 * <p>Fica separado de {@link ProblemaDeFisica} (que \u00e9 apenas o JFrame /
 * janela) de prop\u00f3sito: assim toda a interface pode ser constru\u00edda e
 * testada como um JPanel comum, sem depender de uma janela nativa.</p>
 */
public class PainelPrincipal extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Cena3D cena3D = new Cena3D();
    private final PainelLateral painelLateral = new PainelLateral(cena3D);
    private JLabel descricaoLabel;

    public PainelPrincipal() {
        setLayout(new BorderLayout());
        setBackground(Paleta.FUNDO_JANELA);

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarCorpo(), BorderLayout.CENTER);

        painelLateral.definirOuvinteParametros((q, d) -> atualizarDescricao(q, d));
    }

    // ------------------------------------------------------------
    // Cabe\u00e7alho
    // ------------------------------------------------------------

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Paleta.FUNDO_CABECALHO);
        cabecalho.setBorder(new EmptyBorder(14, 22, 14, 18));

        JLabel titulo = new JLabel("Problema de Cargas \u2014 Simula\u00e7\u00e3o 3D");
        titulo.setFont(Paleta.FONTE_TITULO);
        titulo.setForeground(Color.WHITE);
        cabecalho.add(titulo, BorderLayout.WEST);

        JButton botaoInfo = new JButton("i") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        botaoInfo.setPreferredSize(new java.awt.Dimension(30, 30));
        botaoInfo.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 16));
        botaoInfo.setForeground(Paleta.FUNDO_CABECALHO);
        botaoInfo.setContentAreaFilled(false);
        botaoInfo.setFocusPainted(false);
        botaoInfo.setBorderPainted(false);
        botaoInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botaoInfo.setBorder(BorderFactory.createEmptyBorder());
        botaoInfo.addActionListener(e -> mostrarInformacoesProblema());
        JPanel envoltorioInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        envoltorioInfo.setOpaque(false);
        envoltorioInfo.add(botaoInfo);
        cabecalho.add(envoltorioInfo, BorderLayout.EAST);

        return cabecalho;
    }

    private void mostrarInformacoesProblema() {
        String mensagem =
                "Problema 88 \u2014 Halliday, Resnick e Walker\n\n" +
                "Duas cargas q = +2,0 \u03bcC s\u00e3o mantidas fixas a uma dist\u00e2ncia\n" +
                "d = 2,0 cm uma da outra. O ponto C est\u00e1 localizado acima\n" +
                "do ponto m\u00e9dio entre elas, a uma altura d/2.\n\n" +
                "(a) Com V = 0 no infinito, qual \u00e9 o potencial el\u00e9trico no ponto C?\n" +
                "(b) Qual \u00e9 o trabalho necess\u00e1rio para deslocar uma terceira\n" +
                "     carga q = +2,0 \u03bcC do infinito at\u00e9 o ponto C?\n" +
                "(c) Qual \u00e9 a energia potencial U da nova configura\u00e7\u00e3o?\n\n" +
                "Controles da cena 3D:\n" +
                "  \u2022 Arraste com o bot\u00e3o esquerdo para rotacionar\n" +
                "  \u2022 Use o scroll do mouse para dar zoom\n" +
                "  \u2022 Arraste com o bot\u00e3o direito para mover a c\u00e2mera (pan)";
        JOptionPane.showMessageDialog(this, mensagem, "Sobre este problema", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------
    // Corpo (cena 3D + descri\u00e7\u00e3o + controles | barra lateral)
    // ------------------------------------------------------------

    private JPanel criarCorpo() {
        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setOpaque(false);

        JPanel colunaCena = new JPanel(new BorderLayout());
        colunaCena.setOpaque(false);
        colunaCena.add(cena3D, BorderLayout.CENTER);
        colunaCena.add(criarRodapeCena(), BorderLayout.SOUTH);

        corpo.add(colunaCena, BorderLayout.CENTER);
        corpo.add(painelLateral, BorderLayout.EAST);
        return corpo;
    }

    private JPanel criarRodapeCena() {
        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new GridLayout(2, 1, 0, 10));
        rodape.setBorder(new EmptyBorder(12, 18, 16, 18));

        rodape.add(criarPainelDescricao());
        rodape.add(criarBarraControles());
        return rodape;
    }

    private Cartao criarPainelDescricao() {
        Cartao cartao = new Cartao(new Color(0xEA, 0xF2, 0xFF), new Color(0xCE, 0xE0, 0xFA), 10);
        cartao.setLayout(new BorderLayout());
        descricaoLabel = new JLabel();
        descricaoLabel.setFont(Paleta.FONTE_LABEL);
        descricaoLabel.setForeground(new Color(0x1B, 0x3A, 0x66));
        cartao.add(descricaoLabel, BorderLayout.CENTER);
        atualizarDescricao(2.0, 2.0);
        return cartao;
    }

    private void atualizarDescricao(double q, double d) {
        if (descricaoLabel == null) {
            return;
        }
        String qTexto = formatarNumero(q);
        String dTexto = formatarNumero(d);
        descricaoLabel.setText(
                "<html><body style='width: 520px'>"
                + "Duas cargas positivas iguais (+" + qTexto + " \u03bcC) est\u00e3o separadas por uma "
                + "dist\u00e2ncia d = " + dTexto + " cm. O ponto <b>C</b> est\u00e1 acima do ponto m\u00e9dio "
                + "entre elas, a uma altura d/2."
                + "</body></html>"
        );
    }

    private String formatarNumero(double v) {
        if (v == Math.rint(v)) {
            return String.format(java.util.Locale.US, "%.1f", v);
        }
        return String.valueOf(v);
    }

    private JPanel criarBarraControles() {
        Cartao cartao = new Cartao(Color.WHITE, Paleta.CARTAO_BORDA, 10);
        cartao.setLayout(new BorderLayout());

        JLabel legenda = new JLabel(
                "<html>Arraste: <b>Rotacionar</b> &nbsp;\u2022&nbsp; Scroll: <b>Zoom</b><br>"
                + "Bot\u00e3o direito: <b>Pan</b></html>");
        legenda.setFont(Paleta.FONTE_INFO);
        legenda.setForeground(Paleta.TEXTO_SECUNDARIO);
        cartao.add(legenda, BorderLayout.WEST);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);

        BotaoArredondado botaoVistaPadrao = new BotaoArredondado("Vista Padr\u00e3o", Paleta.AZUL_PRINCIPAL, Paleta.AZUL_CLARO);
        botaoVistaPadrao.addActionListener(e -> cena3D.vistaPadrao());

        BotaoArredondado botaoResetar = new BotaoArredondado("Resetar", Paleta.VERMELHO, Paleta.VERMELHO_ESCURO);
        botaoResetar.addActionListener(e -> {
            cena3D.resetarTudo();
            painelLateral.resetarValoresPadrao();
        });

        botoes.add(botaoVistaPadrao);
        botoes.add(botaoResetar);
        cartao.add(botoes, BorderLayout.EAST);

        return cartao;
    }
}

