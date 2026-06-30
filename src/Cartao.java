import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Painel com cantos arredondados e cor de fundo customizável, usado como
 * "cartão" para agrupar visualmente seções da barra lateral
 * (Parâmetros, Resultados, Informações...).
 */
public class Cartao extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color corFundo;
    private final Color corBorda;
    private final int raio;

    public Cartao(Color corFundo, Color corBorda) {
        this(corFundo, corBorda, 14);
    }

    public Cartao(Color corFundo, Color corBorda, int raio) {
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.raio = raio;
        setOpaque(false);
        setBorder(new EmptyBorder(12, 14, 12, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1.5, getHeight() - 1.5, raio, raio);
        g2.setColor(corFundo);
        g2.fill(forma);
        if (corBorda != null) {
            g2.setColor(corBorda);
            g2.draw(forma);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
