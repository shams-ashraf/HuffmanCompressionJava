import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

class HuffmanNode implements Comparable<HuffmanNode> {
    int freq;
    Character c;
    HuffmanNode left, right;

    HuffmanNode() {
        freq = 0;
    }

    HuffmanNode(Character c, int freq, HuffmanNode left, HuffmanNode right) {
        this.freq = freq;
        this.c = c;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return right == null && left == null;
    }

    @Override
    public int compareTo(HuffmanNode that) {
        return this.freq - that.freq;
    }
}

public class Huffman {
    Reader R;
    PrintWriter W;
    HashMap<Character, Integer> freq = new HashMap<>();
    PriorityQueue<HuffmanNode> pque = new PriorityQueue<>();
    HashMap<Character, String> dict = new HashMap<>(); // compress
    HashMap<String, String> dictionary = new HashMap<>(); // decompress

    public void freq(String In) throws IOException {
        R = new FileReader(In);
        int nextChar;
        while ((nextChar = R.read()) != -1) {
            char key = (char) nextChar;
            if (freq.containsKey(key)) {
                freq.put(key, freq.get(key) + 1);
            } else {
                freq.put(key, 1);
            }
        }
        R.close();
    }

    public void buildTree(HashMap<Character, Integer> freq) {
        for (Character c : freq.keySet()) {
            HuffmanNode node = new HuffmanNode(c, freq.get(c), null, null);
            pque.add(node);
        }

        HuffmanNode root = null;
        while (pque.size() > 1) {
            HuffmanNode left = pque.poll();
            HuffmanNode right = pque.poll();
            root = new HuffmanNode();
            root.freq = left.freq + right.freq;
            root.c = '-';
            root.left = left;
            root.right = right;
            pque.add(root);
        }

        printCode(root, "");
    }

    public void printCode(HuffmanNode root, String s) {
        if (root.isLeaf()) {
            dict.put(root.c, s);
            return;
        }
        printCode(root.left, s + "0");
        printCode(root.right, s + "1");
    }

    public void writeTree(String In) throws IOException {
        W = new PrintWriter(In + "_Dictionary");
        for (Character c : dict.keySet()) {
            W.println(c + " -> " + dict.get(c));
        }
        W.close(); 
    }

    // compress

    public void compress(String In) throws IOException {
        freq(In);            
        buildTree(freq);      
        writeTree(In);         
    
        try (FileReader reader = new FileReader(In);
             DataOutputStream binaryWriter = new DataOutputStream(new FileOutputStream(In + "Compressed"));
             PrintWriter binaryViewWriter = new PrintWriter(In + "BinaryView.txt")) {
    
            int totalBitCount = 0;
            for (Character c : freq.keySet()) {
                totalBitCount += freq.get(c) * dict.get(c).length();
            }
    
            binaryWriter.writeInt(totalBitCount);
    
            int currentByte = 0, bitCount = 0;
            int nextChar;
    
            while ((nextChar = reader.read()) != -1) {
                String code = dict.get((char) nextChar); 
                for (char bit : code.toCharArray()) {
                    currentByte = (currentByte << 1) | (bit - '0'); 
                    binaryViewWriter.print(bit);                  
                    bitCount++;
    
                    if (bitCount == 8) {
                        binaryWriter.write(currentByte);
                        currentByte = 0;
                        bitCount = 0;
                    }
                }
            }
    
            if (bitCount > 0) {
                currentByte <<= (8 - bitCount); 
                binaryWriter.write(currentByte);
            }
        }
    }
    
    // decompress

    public void decompress(String In) throws IOException {
        readTree(In);
    
        try (DataInputStream binaryReader = new DataInputStream(new FileInputStream(In + "Compressed"));
             PrintWriter writer = new PrintWriter(In + "DeCompressed")) {
    
            int encodedLength = binaryReader.readInt();
    
            StringBuilder binaryData = new StringBuilder();
            int nextByte;
            while ((nextByte = binaryReader.read()) != -1) {
                binaryData.append(String.format("%8s", Integer.toBinaryString(nextByte & 0xFF)).replace(' ', '0'));
            }
    
            binaryData.setLength(encodedLength);
    
            StringBuilder word = new StringBuilder();
            for (char bit : binaryData.toString().toCharArray()) {
                word.append(bit);
                if (dictionary.containsKey(word.toString())) { 
                    String decodedChar = dictionary.get(word.toString());
                    writer.print(decodedChar);              
                    word.setLength(0);                    
                }
            }
        }
    }
    
    public void readTree(String In) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(In + "_Dictionary"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] temp = line.split("\\ -> ");
            dictionary.put(temp[1], temp[0]);
        }
        reader.close();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("1. Compress\n2. Decompress");
        Scanner in = new Scanner(System.in);
        int input = in.nextInt();
        Huffman huff = new Huffman();

        if (input == 1) {
            huff.compress("input");
        }

        else if (input == 2) {
            huff.decompress("input");
        }
    }
}
