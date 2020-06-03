// Jeffrey Ramos
// COP 3502, Spring 2019
// je213324

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

class TrieNode
{
	int count;
	TrieNode [] children;
	TrieNode subtrie;

	public TrieNode()
	{
		this.count = 0;
		children = new TrieNode[26];
	}
}


public class SpellCheck implements ActionListener
{
     private TrieNode dictionaryRoot;
     private TrieNode predictRoot;
     private JTextArea text;
     private JScrollPane scroll;
     private JButton sugg1;
     private JFrame frame;
     private JLabel misspell;
     private String str;
     
     private void initComponentsDictionary() throws IOException
     {
        dictionaryRoot = buildTrie("dictionary.txt");

        frame = new JFrame("SpellCheck");
        frame.setSize(new Dimension(1200, 1200));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
// 		button.addActionListener(this);

        sugg1 = new JButton();
        sugg1.setPreferredSize(new Dimension(125, 55));
        sugg1.addActionListener(this);

        misspell = new JLabel();

        text = new JTextArea();
        text.setPreferredSize(new Dimension(500, 500));
        text.setBounds(0, 0, 500, 400);
        text.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        
        text.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {}

            @Override
            public void keyPressed(KeyEvent e)
            {
                final int enter = 10;
                final int space = 32;

                if (e.getKeyCode() == space)
                {
                    str = text.getText();
                    StringBuilder builder = new StringBuilder(str);
                    String prevStr = "";
                    Highlighter highlighter = text.getHighlighter();
                    HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

                    if (str.lastIndexOf(" ") >= 0)
                    {
                        prevStr = str;
                        str = str.substring(str.lastIndexOf(" ") + 1, str.length());
                        System.out.println("str is " + str);

                        if (getTerminalNode(predictRoot, str) != null)
                        {
                            sugg1.setText(getMostFrequentWord(getTerminalNode(predictRoot, str).subtrie, str));
                        }
                        else
                        {
                            sugg1.setText("");
                        }
                    }
                    else
                    {
                        if (containsWord(predictRoot, str) && getTerminalNode(predictRoot, str).subtrie != null)
                        {
                            sugg1.setText(getMostFrequentWord(getTerminalNode(predictRoot, str).subtrie, str));
                        }
                        else
                        {
                            sugg1.setText("");
                        }
                    }

                    prevStr = "";
                    boolean flag = true;
                    str = text.getText();
                    builder = new StringBuilder(str);

                    if (builder.length() > 0)
                    {
                        if (isNumeric(builder.toString()))
                        {
                            System.out.println("1");
                        }
                        else if (builder.toString().indexOf(' ') < 0)
                        {
                            System.out.println("2");
                            System.out.println("With string " + builder.toString() + " we have an index for space of " + builder.toString().indexOf(""));
                            if (!containsWord(dictionaryRoot, builder.toString()))
                            {
                                System.out.println("3");
                                if (!Character.isAlphabetic(builder.charAt(builder.length() - 1)))
                                {
                                    builder.delete(builder.length() - 1, builder.length());
                                }
                                
                                str = text.getText();
                                int p0 = str.indexOf(builder.toString());
                                int p1 = p0 + builder.length();
                               
                                try
                                {
                                    highlighter.addHighlight(p0, p1, painter);
                                }
                                catch (BadLocationException ex)
                                {
                                    Logger.getLogger(SpellCheck.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                System.out.println(builder.toString() + " is not a word.");
                                flag = false;
                            }
                        }
                        else if (isNumeric(builder.substring(0, builder.indexOf(" "))))
                        {
                            System.out.println("4");
                            builder.delete(0, builder.indexOf(" ") + 1);
                        }
                        else if (!containsWord(dictionaryRoot, builder.substring(0, builder.indexOf(" "))))
                        {
                            System.out.println("5");
                            System.out.println(builder.substring(0, builder.indexOf(" ")) + " is not a word.");
                            
                            str = text.getText();
                            int p0 = builder.indexOf(builder.toString());
                            int p1 = p0 + builder.lastIndexOf(" ");
                            try
                            {
                                highlighter.addHighlight(p0, p1, painter);
                            }
                            catch (BadLocationException ex)
                            {
                                Logger.getLogger(SpellCheck.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            flag = false;
                        }
                        else
                        {
                            System.out.println("6");
                            str = builder.substring(builder.toString().lastIndexOf(' ')+1, builder.length());

                            System.out.println("Iserting " + str);
                            predictRoot = insert(predictRoot, str);

                            if (!prevStr.equals("") && prevStr.charAt(prevStr.length() - 1) != '.')
                            {
                                System.out.println("Adding " + str + " to " + prevStr);
                                TrieNode prevStrRoot = getTerminalNode(predictRoot, prevStr).subtrie;
                                getTerminalNode(predictRoot, prevStr).subtrie = insert(prevStrRoot, str);
                                prevStr = str;
                            }
                            else if (prevStr.equals(""))
                            {
                                prevStr = str;
                            }
                            else
                            {
                                prevStr = "";
                            }
                        }

                        builder.delete(0, builder.indexOf(" ") + 1);
                    }
                    

                    if (flag)
                    {
                        insert(predictRoot, builder.toString());

                        System.out.println("No spelling errors detected.");
                    }
                }

                else if (e.getKeyCode() == enter)
                {
                    str = text.getText();
                    StringBuilder builder = new StringBuilder(str);
                    String prevStr = "";
                    boolean flag = true;

                    while (builder.length() > 0)
                    {
                        if (isNumeric(builder.toString()))
                        {
                            break;
                        }

                        if (builder.toString().indexOf(' ') < 0)
                        {
                            if (!containsWord(dictionaryRoot, builder.toString()))
                            {
                                if (!Character.isAlphabetic(builder.charAt(builder.length() - 1)))
                                {
                                    builder.delete(builder.length() - 1, builder.length());
                                }

                                System.out.println(builder.toString() + " is not a word.");
                                flag = false;
                            }
                            else
                            {
                                if (prevStr.equals(""))
                                {
                                    predictRoot = insert(predictRoot, builder.toString());
                                    System.out.println(getTerminalNode(predictRoot, builder.toString()).count);
                                    break;
                                }
                                else
                                {
                                    builder.append(" ");
                                    str = builder.substring(0, builder.indexOf(" "));
                                    System.out.println("prevStr = " + prevStr);
                                    TrieNode prevStrRoot = getTerminalNode(predictRoot, prevStr).subtrie;
                                    getTerminalNode(predictRoot, prevStr).subtrie = insert(prevStrRoot, str);
                                    prevStr = str;
                                }
                            }

                            break;
                        }

                        else if (isNumeric(builder.substring(0, builder.indexOf(" "))))
                        {
                            builder.delete(0, builder.indexOf(" ") + 1);
                            continue;
                        }

                        else if (!containsWord(dictionaryRoot, builder.substring(0, builder.indexOf(" "))))
                        {
                            System.out.println(builder.substring(0, builder.indexOf(" ")) + " is not a word.");
                            flag = false;
                        }
                        else
                        {
                            str = builder.substring(0, builder.indexOf(" "));

                            System.out.println("Inserting " + str);
                            predictRoot = insert(predictRoot, str);

                            if (!prevStr.equals("") && prevStr.charAt(prevStr.length() - 1) != '.')
                            {
                                System.out.println("Adding " + str + " to " + prevStr);
                                TrieNode prevStrRoot = getTerminalNode(predictRoot, prevStr).subtrie;
                                getTerminalNode(predictRoot, prevStr).subtrie = insert(prevStrRoot, str);
                                prevStr = str;
                            }
                            else if (prevStr.equals(""))
                            {
                                prevStr = str;
                            }
                            else
                            {
                                prevStr = "";
                            }
                        }
                        
                        builder.delete(0, builder.indexOf(" ") + 1);
                    }

                    if (flag)
                    {
                        insert(predictRoot, builder.toString());

                        System.out.println("No spelling errors detected.");
                    }

//                    printTrie(predictRoot, true);
//                    atHelper(predictRoot, "I", 40);
                    System.out.println("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {}
        });
        
        scroll = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(300, 25, 500, 500);
        
        frame.add(sugg1);
//        frame.add(text);
        frame.add(scroll);
        frame.add(misspell);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent event)
    {
        if (sugg1.getText().equals(""))
        {
            return;
        }
        
        text.setText(text.getText() + getMostFrequentWord(getTerminalNode(predictRoot, str).subtrie, str) + " ");

        if (str.lastIndexOf(" ") >= 0)
        {
            str = str.substring(str.lastIndexOf(" ") + 1, str.length());
        }

        System.out.println("str is " + str);

        System.out.println(getMostFrequentWord(getTerminalNode(predictRoot, str).subtrie, str));
        str = getMostFrequentWord(getTerminalNode(predictRoot, str).subtrie, str);
        sugg1.setText(getMostFrequentWord(getTerminalNode(predictRoot, str).subtrie, str));
    }

    // Code found on: https://www.baeldung.com/java-check-string-number
    public static boolean isNumeric(String strNum)
    {
        if (strNum == null)
        {
            return false;
        }
        
        try
        {
            double d = Double.parseDouble(strNum);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }

    public static int semiLastIndexOf(String str, char semiLast)
    {
        int count = 0;

        for (int i = str.length() - 1; i >= 0; i--)
        {
            if (str.charAt(i) == semiLast)
            {
                if (count < 2)
                {
                    count++;
                }
                
                if (count == 2)
                {
                    return i;
                }
            }
        }

        return -1;
    }

    // Insert a string into a trie. This function returns the root of the trie.
    public TrieNode insert(TrieNode root, String str)
    {
        TrieNode temp;
        str = str.toLowerCase();

        if (root == null)
        {
            root = new TrieNode();
        }

        if (str.equals(""))
        {
            return root;
        }

        temp = root;

        for (int i = 0; i < str.length(); i++)
        {            
            if (!Character.isAlphabetic(str.charAt(i)) && str.charAt(i) == '-')
            {
                continue;
            }
            else if (!Character.isAlphabetic(str.charAt(i)) && str.charAt(i) != '-')
            {
                return root;
            }

            int index = (int) str.charAt(i) - 'a';

            // Before the wizard can move forward to the next node, (s)he needs to
            // make sure that node actually exists. If not, create it!
            if (temp.children[index] == null)
            {
                temp.children[index] = new TrieNode();
            }

            // Now the wizard is able to jump forward.
            temp = temp.children[index];
        }

        // When we break out of the for-loop, the wizard should be at the terminal
        // node that represents the string we're trying to insert.
        temp.count++;
        return root;
    }

    public void createSubtrie(TrieNode root, String word)
    {
        insert(root.subtrie, word);
    }

    // Helper function called by printTrie().
//    public void printTrieHelper(TrieNode root, StringBuilder buffer, int k)
//    {
//        if (root == null)
//        {
//            return;
//        }
//
//        if (root.count > 0)
//        {
//            System.out.println(buffer.toString() + " (" + root.count + ")");
//        }
//
//        for (int i = 0; i < 26; i++)
//        {
//            buffer.append((char) ('a' + i));
//            printTrieHelper(root.children[i], buffer, k + 1);
//            buffer.delete(k, buffer.length());
//        }
//    }

    // If printing a subtrie, the second parameter should be true; otherwise, if
    // printing the main trie, the second parameter should be false. (Credit: Dr. S.)
//    public void printTrie(TrieNode root, boolean useSubtrieFormatting)
//    {
//        StringBuilder buffer = new StringBuilder();
//
//        if (useSubtrieFormatting)
//        {
//            buffer.append("- ");
//            printTrieHelper(root, buffer, 2);
//        }
//        else
//        {
//            printTrieHelper(root, buffer, 0);
//        }
//    }

    // Returns the terminal node of a string within a trie.
    public TrieNode getTerminalNode(TrieNode root, String str)
    {
        int index;
        str = str.toLowerCase();
        TrieNode temp = root;

        if (root == null)
        {
            return null;
        }

        // Finds the terminal node of "str" in the trie by looping through the length of the string.
        for (int i = 0; i < str.length(); i++)
        {
            // If the string contains a non-alphabetical character, we don't include it in our traversal
            // through the trie.
            if (!Character.isAlphabetic(str.charAt(i)))
            {
                continue;
            }

            index = (int) (str.charAt(i) - 'a');

            // If the index of the corresponding child is non-NULL, then we set the our traversal variable
            // "temp" equal to that child.
            if (temp.children[index] != null)
            {
                temp = temp.children[index];
            }
        }

        return temp;
    }

    // Creates a trie and inserts all words within a file into it.
    public TrieNode buildTrie(String filename) throws IOException
    {
        TrieNode root = null;
        TrieNode terminal = null;
        String str = "";
        Scanner fileReader = new Scanner(new File(filename));
        int length = 0;

        while (fileReader.hasNext())
        {
            str = fileReader.next();

            // Insert the current word into the trie.
            root = insert(root, str);

            // Determines whether or not the current string is the first word of the string.
            // If it is, then there is no subtrie for this first word to go in.
            if (terminal != null)
            {
                // Inserts the current word into the subtrie of the previous word.
                terminal.subtrie = insert(terminal.subtrie, str);

                // If the last character of the current string is a punctuator that signals the end of a
                // sentence, we reset the "terminal" variable back to NULL and go onto the next word.
                if (str.charAt(str.length() - 1) == '.' || str.charAt(str.length() - 1) == '!'
                        || str.charAt(str.length() - 1) == '?')
                {
                    terminal = null;
                    continue;
                }
            }

            // Set the "terminal" TrieNode pointer equal to the terminal node of the string.
            terminal = getTerminalNode(root, str);
        }

        fileReader.close();
        return root;
    }

//    // Prints a word from a trie followed by the word that most frequently follows it in the
//    // corpus used.
//    public void atHelper(TrieNode root, String str, int n)
//    {
//        if (root == null)
//        {
//            return;
//        }
//
//        // Prints the current string. Also, prints a space after it if it's not the last word being
//        // printed, or an empty string if it is.
//        System.out.format("%s%s", str, (n > 0 && getTerminalNode(root, str).subtrie != null) ? " " : "");
//
//        // If the number of times we need to print has not been reached and if the current string does
//        // have a subtrie, then we find the word that most frequently follows the current string.
//        // (We search for the string that was most frequently inserted into the current string's subtrie)
//        if (n > 0 && getTerminalNode(root, str).subtrie != null)
//        {
//            // Finds the most frequent word in the current string's subtrie.
//            str = getMostFrequentWord(getTerminalNode(root, str).subtrie, str);
//            atHelper(root, str, n - 1);
//        }
//
//    }

    // Returns the bigger of two numbers passed in as parameters.
    public int max(int a, int b)
    {
        return (a > b) ? a : b;
    }

    // Searches the trie for the node with the biggest count and returns that maximum value.
    public int findMaxCount(TrieNode root, int maxVal)
    {
        TrieNode temp = root;

        if (root == null)
        {
            return maxVal;
        }

        // If the count of the current node is greater than or equal to 1, then we find the maximum value
        // between "maxVal" and the "count" member. If so, we replace the value stored at "maxVal" with
        // the new greater value, that being the value that the "count" member of the node we are at.
        if (temp.count >= 1)
        {
            maxVal = max(maxVal, root.count);
        }

        // We loop through each child of the current node to search for the biggest value.
        for (int i = 0; i < 26; i++)
        {
            maxVal = findMaxCount(temp.children[i], maxVal);
        }

        return maxVal;
    }

    // Searches for the most common word in the trie in alphabetical order and copies that word into the
    // string "str" passed into the parameters.
    public String searchMostCommon(TrieNode root, StringBuilder buffer, int maxVal, Integer found)
    {
        TrieNode temp = root;

        if (root == null)
        {
            return "";
        }

        // Checks if we found the node with the maximum "count" value and if we haven't already found a
        // node with the same maximum "count" value. If so, we copy what was in "buffer" into "str" and
        // sets *found to 1 to indicate that we found the word that occurs most frequently.
        if (temp.count == maxVal && found != 1)
        {
            found = 1;
            return buffer.toString();
        }

        for (int i = 0; i < 26; i++)
        {
            // If the child we are looking at is non-null, then we go there and insert the corresponding
            // letter into str. We then look at all of its children and continue to search for letters to
            // insert into "buffer".
            temp = temp.children[i];
            buffer.append((char) ('a' + i));

            String str = searchMostCommon(temp, buffer, maxVal, found);

            // Makes a recursive call with an index that is 1 greater than it was before in order to insert
            // into the next index in "buffer".
            if (!str.equals(""))
            {
                return str;
            }

            // Reset our traversal TrieNode pointer variable back to the root, or the terminal node of the
            // prefix.
            buffer.delete(buffer.length() - 1, buffer.length());
            temp = root;
        }

        // We then reset "buffer" to what it was before searching, which handles the
        // case where we didn't find the word and we want to "erase" what we inserted, by inserting a
        // null sentinel at the current index.
        return "";
    }

    // Copies the the word that was most frequently inserted into the trie into the string "str" passed
    // into the parameters.
    public String getMostFrequentWord(TrieNode root, String str)
    {
        StringBuilder buffer = new StringBuilder();

        if (root == null)
        {
            return "";
        }

        // Parameters are as follows: TrieNode *root, char *str, char *buffer, int maxVal, int index,
        // int *found. "maxVal" (the second parameter in the findMaxCount function call) is 0 because the
        // maximum value should be 0 until a bigger vale is found. "index" is also 0 because we are
        // inserting into "buffer" starting from the first index in "buffer". The string "buffer"
        // contains will be copied into str within the function call.
        return searchMostCommon(root, buffer, findMaxCount(root, 0), 0);
    }

    // Returns 1 if "str" was found in the trie. Returns 0 otherwise.
    public boolean containsWord(TrieNode root, String str)
    {
        int index;
        TrieNode temp = root;
        str = str.toLowerCase();

        // Loops through the trie in order to attempt to find "str" in the trie.
        for (int i = 0; i < str.length(); i++)
        {
            // If we reach a NULL node, the word must not be in the trie.
            if (temp == null)
            {
                return false;
            }

            if (!Character.isAlphabetic(str.charAt(i)))
            {
                continue;
            }

            index = (int) (str.charAt(i) - 'a');
            temp = temp.children[index];
        }

        // If we reached the last node in the path we take trying to find "str" in the trie and it
        // contains a count greater than 0, that means that we word is, in fact, in the trie.
        if (temp != null && temp.count >= 1)
        {
            return true;
        }

        // Otherwise, we return 0.
        return false;
    }

    // A helper function that returns the number of times that a word beginning with a prefix is found
    // by searching for all words that come out from the terminal node at the end of the prefix string.
    public int searchPrefixHelper(TrieNode root)
    {
        TrieNode temp = root;
        int count = 0;

        if (root == null)
        {
            return 0;
        }

        // We add the value stored in the "count" member of the TrieNode to the "count" variable.
        count += temp.count;

        // Loops through all the children to find any words that begin with the prefix.
        for (int i = 0; i < 26; i++)
        {
            // If the letter we are analyzing is in the trie, we go to the node that the letter's path leads
            // to. We also add all values stored within each "count" member of each TrieNode.
            if (temp.children[i] != null)
            {
                temp = temp.children[i];
                count += searchPrefixHelper(temp);
            }

            // Reset our traversal TrieNode pointer variable back to the root, or the terminal node of the
            // prefix.
            temp = root;
        }

        return count;
    }

    // Returns the number of times that a word beginning with a prefix is found.
    public boolean prefixCount(TrieNode root, String str)
    {
        TrieNode temp = root;
        int index;
        str = str.toLowerCase();

        // If the root is NULL, there cannot be any wrds that begin with the prefix in the trie so we
        // return 0.
        if (root == null)
        {
            return false;
        }

        // Moves the temp pointer to the terminal node of the prefix.
        for (int i = 0; i < str.length(); i++)
        {
            if (temp == null)
            {
                return false;
            }

            if (!Character.isAlphabetic(str.charAt(i)))
            {
                continue;
            }

            index = (int) (str.charAt(i) - 'a');

            temp = temp.children[index];
        }

        if (searchPrefixHelper(temp) > 0)
        {
            return true;
        }

        return false;
    }

    // Creates a trie and calls the processInputFile function in order to complete actions using that
    // trie.
    public static void main(String[] args) throws IOException
    {        
        SpellCheck sp = new SpellCheck();
        sp.initComponentsDictionary();
    }
}
