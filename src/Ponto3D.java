/**
 * Representa um ponto (ou vetor) no espaço tridimensional.
 *
 * Classe simples e imutável usada por toda a cena 3D para guardar
 * coordenadas em metros no "mundo" da simulação (antes da projeção
 * para a tela, que é responsabilidade da classe {@link Camera3D}).
 */
public class Ponto3D {

    public final double x;
    public final double y;
    public final double z;

    public Ponto3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Ponto3D somar(Ponto3D outro) {
        return new Ponto3D(x + outro.x, y + outro.y, z + outro.z);
    }

    public Ponto3D escalar(double fator) {
        return new Ponto3D(x * fator, y * fator, z * fator);
    }

    public double distancia(Ponto3D outro) {
        double dx = x - outro.x;
        double dy = y - outro.y;
        double dz = z - outro.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "(%.4f, %.4f, %.4f)", x, y, z);
    }
}
