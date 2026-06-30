import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 * Classe principal da aplicação.
 *
 * Simulação 3D interativa do Problema 88 (Halliday, Resnick e Walker —
 * capítulo de Potencial Elétrico): duas cargas iguais e fixas, separadas
 * por uma distância d, com um ponto C acima do ponto médio entre elas, a
 * uma altura d/2. A aplicação calcula o potencial elétrico em C, o
 * trabalho necessário para trazer uma terceira carga do infinito até C, e
 * a energia potencial da nova configuração — tudo isso sobre uma cena 3D
 * construída inteiramente com Java2D/Swing (sem bibliotecas gráficas
 * externas, sem Java3D, sem OpenGL).
 *
 * Esta classe é apenas a "janela" (JFrame): toda a interface de fato é
 * montada em {@link PainelPrincipal}, o que mantém a UI testável de forma
 * independente da janela do sistema operacional.
 */
public class ProblemaDeFisica extends JFrame {

    private static final long serialVersionUID = 1L;

    public ProblemaDeFisica() {
        setTitle("Problema de Cargas - Simulação 3D");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        setContentPane(new PainelPrincipal());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProblemaDeFisica().setVisible(true));
    }
}
