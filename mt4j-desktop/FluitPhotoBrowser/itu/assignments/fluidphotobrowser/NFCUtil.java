package itu.assignments.fluidphotobrowser;


import java.nio.ByteBuffer;
import java.util.List;
import java.util.Observable;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

public class NFCUtil extends Observable implements Runnable {
	
	private boolean isUserAuthenticated;
	private String activeUser;
	private String currentCardBalance;
	
	public String getActiveUser() {
		return activeUser;
	}
	public boolean isUserAuthenticated() {
		return isUserAuthenticated;
	}

	public CardTerminal InitializeTerminal() throws CardException
	{
		//Get terminal
		System.out.println("Searching for terminals...");
		CardTerminal terminal = null;
		TerminalFactory factory = TerminalFactory.getDefault();
		List<CardTerminal> terminals = factory.terminals().list();
		
		//Print list of terminals
		for(CardTerminal ter:terminals)
		{
			System.out.println("Found: "  +ter.getName().toString());
			terminal = terminals.get(0);// We assume just one is connected
		}
		return terminal;
	}
	
	public boolean IsCardPresent(CardTerminal terminal) throws CardException
	{
		System.out.println("Waiting for card...");
		
		boolean isCard = false;
		
		while (!isCard)
		{
			isCard = terminal.waitForCardPresent(0);
			if(isCard) System.out.println("Card was found ! :-)");
		}
		return true;
	}
	
	public CardChannel GetCardAndOpenChannel(CardTerminal terminal) throws CardException
	{
		Card card = terminal.connect("*");
		CardChannel channel = card.getBasicChannel();
		
		byte[] baReadUID = new byte[5];
		baReadUID = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00}; //FF CA 00 00 00
		
		System.out.println("UID: " + SendCommand(baReadUID, channel));
		
		return channel;
	}
	
	public boolean LoadBasicKey(CardChannel channel) // LOADS 6BIT KEY: FF FF FF FF FF FF
	{
		byte[] baLoadKey = new byte[12];
		baLoadKey = new byte[]{(byte) 0xFF, (byte) 0x82, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}; //FF 82 00 00 06 [FF FF FF FF FF FF]
		
		if(SendCommand(baLoadKey, channel).equals("9000")) {
			System.out.println("Key succefully loaded to the reader!! ");
			return true;
		} else {
			return false;
		}
	}
	
	public boolean AuthenticateBlock1(CardChannel channel)
	{
		byte[] baAuth = new byte[10];
		baAuth = new byte[]{(byte) 0xFF, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x60, (byte) 0x00}; //FF 86 00 00 05 01 00 01 60 00 
		
		if(SendCommand(baAuth, channel).equals("9000")) { 
			System.out.println("Authentication for block 0x01 succeded! ");
			return true;
		} else {
			System.out.println("Authentication failed!");
			return false;
		}
	}
	
	public boolean AuthenticateBlock2(CardChannel channel)
	{
		byte[] baAuth = new byte[10];
		baAuth = new byte[]{(byte) 0xFF, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x00}; //FF 86 00 00 05 01 00 01 60 00 
		
		if(SendCommand(baAuth, channel).equals("9000")) { 
			System.out.println("Authentication for block 0x02 succeded! ");
			return true;
		} else {
			System.out.println("Authentication failed!");
			return false;
		}
	}
	
	
	public static String SendCommand(byte[] cmd, CardChannel channel)
	{
		String response = "";
		byte[] baResp = new byte[258];
		
		ByteBuffer bufCmd = ByteBuffer.wrap(cmd);
		ByteBuffer bufResp = ByteBuffer.wrap(baResp);
		
		int output = 0;
		
		try{
			output = channel.transmit(bufCmd, bufResp);
		}
		catch(CardException ex){
			ex.printStackTrace();
		}
		
		for (int i = 0; i < output; i++) {
			response += String.format("%02X", baResp[i]);
		}
		return response;	
	}

	@Override
	public void run() {
		
		//Authenticate
		CardTerminal terminal;
		CardChannel channel;
		try {
			terminal = this.InitializeTerminal();
			if(this.IsCardPresent(terminal))
			{
				channel = this.GetCardAndOpenChannel(terminal); //Returns CarChannel object (TO DO: put and store inside class)
				this.LoadBasicKey(channel);
				this.AuthenticateBlock1(channel);
				
				
				byte[] baRead = new byte[5];
				baRead = new byte[]{(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x01, (byte) 0x10}; //Read 16bits from block 01
				
				String hexUser = this.SendCommand(baRead, channel);
				hexUser = hexUser.substring(0, hexUser.length() - 4);
				String stringUser = HexStringConverter.getHexStringConverterInstance().hexToString(hexUser);
				stringUser = stringUser.replace(".", "");

				
				String desiredUser = "WIKTOR";
				System.out.println(stringUser);
				
				if(stringUser.equals(desiredUser)){
					this.isUserAuthenticated = true;
					this.activeUser = stringUser;
					setChanged();
                	notifyObservers();
				} else {
					this.isUserAuthenticated = false;
					setChanged();
                	notifyObservers();
                	Thread.sleep(3000L);
                	run();
				}
			}
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	public String getCardBalance(){
		CardTerminal terminal;
		CardChannel channel;
		String currentBalanceString;
		
		try{
			terminal = this.InitializeTerminal();
			
			if(this.IsCardPresent(terminal)){
				channel = this.GetCardAndOpenChannel(terminal); //Returns CarChannel object (TO DO: put and store inside class)
				this.AuthenticateBlock2(channel);
				
				byte[] baRead = new byte[5];
				baRead = new byte[]{(byte) 0xFF, (byte) 0xB1, (byte) 0x00, (byte) 0x02, (byte) 0x04};
				
				String currentBalanceHex = this.SendCommand(baRead, channel);
				System.out.println(currentBalanceHex);
				currentBalanceHex = currentBalanceHex.substring(0, currentBalanceHex.length() - 4);
				System.out.println(currentBalanceHex);
				currentBalanceString = HexStringConverter.getHexStringConverterInstance().hexToDecimalAsString(currentBalanceHex);
				this.currentCardBalance = currentBalanceString;
				System.out.println("Current card balance is: " + currentBalanceString);
			}
			
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.currentCardBalance;
	}
	
	public String decrementBalance(int amount) throws InterruptedException
	{
		CardTerminal terminal;
		CardChannel channel;
		String currentBalanceString;
		String amountStr = "0x0" + amount;
		
		try{
			terminal = this.InitializeTerminal();
			
			if(this.IsCardPresent(terminal)){
				channel = this.GetCardAndOpenChannel(terminal); //Returns CarChannel object (TO DO: put and store inside class)
				this.AuthenticateBlock2(channel);
				
				byte[] baDecrement = new byte[10];
				baDecrement = new byte[]{(byte) 0xFF, (byte) 0xD7, (byte) 0x00, (byte) 0x02, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) Byte.decode(amountStr)};
				this.SendCommand(baDecrement, channel);
				
				Thread.sleep(2000L);
				
				
				byte[] baRead = new byte[5];
				baRead = new byte[]{(byte) 0xFF, (byte) 0xB1, (byte) 0x00, (byte) 0x02, (byte) 0x04};
				
				String currentBalanceHex = this.SendCommand(baRead, channel);
				System.out.println(currentBalanceHex);
				currentBalanceHex = currentBalanceHex.substring(0, currentBalanceHex.length() - 4);
				System.out.println(currentBalanceHex);
				currentBalanceString = HexStringConverter.getHexStringConverterInstance().hexToDecimalAsString(currentBalanceHex);
				this.currentCardBalance = currentBalanceString;
				System.out.println("Current card balance is: " + currentBalanceString);
			}
			
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.currentCardBalance;
		
	}
	
	
}
		
	
	


