
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author RUTH
 */
public class JodoDaVelhaServidor {
   
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8901);
            System.out.println("[SERVIDOR] Jogo da Velha ativo!");
 
        try {
             while (true) {
                 Game game = new Game();

                 Game.Player playerX = game.new Player(listener.accept(), 'X');
                 Game.Player playerO = game.new Player(listener.accept(), 'O');

                 playerX.setOpponent(playerO);
                 playerO.setOpponent(playerX);
                 game.currentPlayer = playerX;
 
                 playerX.start(); 
                 playerO.start();
 }
            }
            finally {
             listener.close();
                }
    }
}
 class Game {
 /**
 * O TABULEIRO possui 9 espaços em branco
 * Cada um dos quadrados será reivindicado por um jogador até que não estejam mais vazios.
 * Criamos uma matriz onde NULL representa um espaço que ainda não foi reivindicado, quando 
 * isso ocorre o espaço passa a ser referenciado pelo jogador que o marcou.
 */
     private Player[] board = {
         null, null, null,
         null, null, null,
         null, null, null};
 /**
 * JOGADOR ATUAL.
 */
 Player currentPlayer;
 /**
 *Retorna se o estado atual do tabuleiro é de que um dos players venceu.
 */
 public boolean hasWinner() {
 return
 (board[0] != null && board[0] == board[1] && board[0] == board[2])
 ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
 ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
 ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
 ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
 ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
 ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
 ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
 }
 /**
 *Retorna se ainda existe algum espaço vazio.
 */
 public boolean boardFilledUp() {
     for (int i = 0; i < board.length; i++) {
        if (board[i] == null) {
            return false;
        }
     }
        return true;
 }
 /**
 Chamado pela Thread do jogador quando ele tenta fazer uma jogada.
 Essa Thread checa se a tentativa é válida, ou seja, o jogador que está requisitando a jogada deve ser 
 * o jogador atual e o espaço que ele estiver marcando deve ser um ainda não ocupado.
 Se a jogada for válida o estado atual do jogo muda, o outro jogador se torna o jogador atual e é avisado sobre
 * qual jogada foi feita para que possa atualizar seu Cliente.
 */
 public synchronized boolean legalMove(int location, Player player) {
    if (player == currentPlayer && board[location] == null) {
        board[location] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
        currentPlayer.otherPlayerMoved(location);
             return true;
    }
             return false;
 }
 /**
 * O jogador é identificado por um marcador, que é X ou O.
 * Para se comunicar com o cliente, o jogador tem um socket que possui leitor e 
 * escrita já que estamos comunicando somente de texto
 */
 class Player extends Thread {
    char mark;
    Player opponent;
    Socket socket;
    BufferedReader input;
    PrintWriter output;
 /**
 * Constói um Thread para manipular um determinado Socket.
 * Mark inicializa os campos de fluxo
 * As duas primeiras mensagens são exibidas: BEM-VINDO e AGUARDANDO SEU OPONENTE
 * welcoming messages.
 */
 public Player(Socket socket, char mark) {
    this.socket = socket;
    this.mark = mark;
        try {
            input = new BufferedReader(
            
                new InputStreamReader(socket.getInputStream()));
           
                    output = new PrintWriter(socket.getOutputStream(), true);
                    output.println("BEM VINDO " + mark);
                    output.println("MESSAGE Aguardando por seu oponente");
        } catch (IOException e) {
                    
                    System.out.println("Player died: " + e);
        }
 }
 /**
 * Aceita o oponente.
 */
 public void setOpponent(Player opponent) {
        this.opponent = opponent;
 }
 /**
 * Informa que o outro jogador vez uma jogada
 */
 public void otherPlayerMoved(int location) {
        output.println("OPPONENT_MOVED " + location);
        output.println(
 
                hasWinner() ? "DEFEAT" : boardFilledUp() ? "EMPATE" : "");
 }
 /**
 * Método RUN
 */
 public void run() {
    try {
 // Inicia apenas quando todos os jogadores conectarem
        output.println("MESSAGE All players connected");
 // Fala para o primeiro jogador que é a vez dele
    if (mark == 'X') {
        output.println("MESSAGE Your move");
 }
 // Recebe e processa comandos do Cliente.
 while (true) {
        String command = input.readLine();
            if (command.startsWith("MOVE")) {
        
        int location = Integer.parseInt(command.substring(5));
            if (legalMove(location, this)) {
 
                output.println("VALID_MOVE");
                output.println(hasWinner() ? "VICTORY": boardFilledUp() ? "TIE": "");

            } else {
                output.println("MESSAGE ?");

            } } else if (command.startsWith("QUIT")) {
                return;
                }
            }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            } finally {
                try {socket.close();} catch (IOException e) {}
 }
 }
 }
}

