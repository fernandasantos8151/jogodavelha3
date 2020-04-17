
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author RUTH
 */
public class JogoDaVelhaCliente {
    private JFrame frame = new JFrame("Jogo Da Velha");
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;
    private Square[] board = new Square[9];
    private Square currentSquare;
    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
 /**
 * CONSTRUÇÃO DO CLIENTE SE COMUNICANDO COM O SERVIDOR
 * LAYOUT DO GUI
 * REFERENCIANDO GUI LISTENERS
 */
        public JogoDaVelhaCliente(String serverAddress) throws Exception {
 // Configurando a rede
             socket = new Socket(serverAddress, PORT);
             in = new BufferedReader(new InputStreamReader(
             socket.getInputStream()));
             out = new PrintWriter(socket.getOutputStream(), true);
 // Layout GUI

     messageLabel.setBackground(Color.lightGray);
     frame.getContentPane().add(messageLabel, "South");
 
     JPanel boardPanel = new JPanel();
     boardPanel.setBackground(Color.black);
     boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
 
     for (int i = 0; i < board.length; i++) {
        final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {

    public void mousePressed(MouseEvent e) {
        currentSquare = board[j];
        out.println("MOVE " + j);}});
         boardPanel.add(board[i]);
        }
         frame.getContentPane().add(boardPanel, "Center");
 }
 /**
 A Thread principal do cliente vai ouvir mensagens do servidor. A primeira será uma mensagem de "BEM-VINDO", aqui
 * será onde o jogador recebe o ícone que irá usar (X ou O)
 Depois o código entrará em um looping esperando as seguintes respostas: MOVIMENTO VÁLIDO, OPONENTE JOGOU, VITÓRIA,
 * DERROTA, EMPATE, OPONENTE SAIU ou MENSAGEM.
 Quando ocorrer VITÓRIA, DERROTA ou EMPATE, o jogo vai perguntar se o jogador deseja iniciar nova rodada
 * Se a resposta for SIM: continua no looping.
 * Se a resposta for NÃO: o código sai do looping o servidor envia uma mensagem escrita QUIT (SAIU). O mesmo ocorre
 * se um jogador sair da partida antes que ela acabe.
 */
 public void play() throws Exception {
    String response;
        try {
            response = in.readLine();
        
        if (response.startsWith("BEM-VINDO")) {

            char mark = response.charAt(8);
            icon = new ImageIcon(mark == 'X' ? "X.png" : "O.png");
            opponentIcon = new ImageIcon(mark == 'X' ? "O.png" : "X.png");
            frame.setTitle("Jogo da Velha - Player " + mark);
        }
        
        while (true) {
            response = in.readLine();
 
        if (response.startsWith("VALID_MOVE")) {
 
            messageLabel.setText("Valid move, please wait");
            currentSquare.setIcon(icon);
            currentSquare.repaint();
         
        } else if (response.startsWith("OPPONENT_MOVED")) {
            int loc = Integer.parseInt(response.substring(15));
            board[loc].setIcon(opponentIcon);
            board[loc].repaint();
            messageLabel.setText("Opponent moved, your turn");
 
        } else if (response.startsWith("VICTORY")) {
            messageLabel.setText("You win");
            break;
 
        } else if (response.startsWith("DEFEAT")) {
            messageLabel.setText("You lose");
            break;
            
        } else if (response.startsWith("TIE")) {
            messageLabel.setText("You tied");
            break;

        } else if (response.startsWith("MESSAGE")) {
            messageLabel.setText(response.substring(8));
        }
 }
             out.println("QUIT");
 }
        
 finally {
        socket.close();
 }
 }
 private boolean wantsToPlayAgain() {
 int response = JOptionPane.showConfirmDialog(frame, "Quer jogar de novo?", "Jogo top top top", JOptionPane.YES_NO_OPTION);
    frame.dispose();
        return response == JOptionPane.YES_OPTION;
 }
 /**
 CRIAÇÃO DOS ESPAÇOS GRÁFICOS NA JANELA DO CLIENTE
 * Cada espaço é um painel em branco. Quando o cliente clicar em um dos espaços estará chamando setIcon(); 
 * para preencher o espaço com o ícone que o representa (X ou O).
 */
 
 static class Square extends JPanel {
    JLabel label = new JLabel((Icon)null);
 
    public Square() {
        setBackground(Color.white);
        add(label);
 }
 
    public void setIcon(Icon icon) {
        label.setIcon(icon);
 }
 }
 /**
 *Rodando o cliente como uma aplicação
 */
 
 public static void main(String[] args) throws Exception {
    while (true) {
        String serverAddress = (args.length == 0) ? "localhost" : args[1];
        JogoDaVelhaCliente client = new JogoDaVelhaCliente(serverAddress);
        
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setSize(400, 400);
        client.frame.setVisible(false);
        client.frame.setResizable(false);
        client.play();
 
        if (!client.wantsToPlayAgain()) {
            break;
 }
 }
 }
}
    
