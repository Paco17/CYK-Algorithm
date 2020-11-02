import java.awt.image.IndexColorModel;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/*
 * Francisco Javier Ramos
 * A01636425
 * 01/11/2020*/
public class CYK_Algorithm {

	private Map<Character, ArrayList<String>> gramaticas;
	private String cadena;

	public CYK_Algorithm(String[] input, String cadena) {
		
		this.gramaticas = doProductions(input);
		this.cadena = cadena;
		
		if(verificarChomsky(gramaticas)) {
			System.out.println("Gramatica está en Forma Normal de Chomsky");
			if(CYK(gramaticas, cadena)) {
				System.out.println("La cadena pertenece al lenguaje de la gramatica");
			}else {
				System.out.println("La cadena no pertenece al lenguaje de la gramatica");
			}
		}else {
			chomsky(gramaticas);
			System.out.println("La gramatica ahora está en Forma Normal de Chomsky");
			if(CYK(gramaticas, cadena)) {
				System.out.println("\n\nLa cadena pertenece al lenguaje de la gramatica");
			}else {
				System.out.print("La cadena no pertenece al lenguaje de la gramatica");
			}
		}
	}
	
	private boolean verificarChomsky(Map<Character, ArrayList<String>> gramaticas) {
		//Verificacion de forma Normal
		for(Map.Entry<Character, ArrayList<String>> entry: gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			for(String str: producciones) {
				if(str.length()>=3) {
					return false;
				}
				
				int noTerminales = 0;
				
				for(int i=0;i<str.length();i++) {
					//Epsilon
					if(str.charAt(i)=='e' && str.length()==1) {
						return false;
					}
					
					//Terminales deben estar solos
					if(str.charAt(i)>97 && str.length()>1) {
						return false;
					}
					
					//Contador de producciones A->Z
					if(str.charAt(i)>64 && str.charAt(i)<95) 
						noTerminales++;
					
					
					//Mas de dos producciones o producciones unitarias
					if(noTerminales>2 ||  (noTerminales==1 && str.length()==1)) {
						return false;
					}
					
					//Producciones inutiles
					if((str.charAt(0) == entry.getKey().charValue() && str.length()==1) || (str.charAt(0) == entry.getKey().charValue() && str.charAt(1) == entry.getKey().charValue())){
						return false;
					}
				}
			}
		}return true;
	}
	
	private Map<Character, ArrayList<String>> doProductions(String[] input){
		if(input == null) {
			return null;
		}
		
		Map<Character, ArrayList<String>> gramaticas = new Hashtable<>();
		for(String str: input) {
			/*Simulando que en el input ninguna produccion aparece > 1 vez
			 * en cuestión de producir diferente en casillas apartadas
			 */
			
			if(!gramaticas.containsKey(str.charAt(0))) {
				String produccion = str.substring(4);
				gramaticas.put(str.charAt(0), new ArrayList<>(Arrays.asList(produccion.split("\\|"))));
			}
		}
		
		return gramaticas;
		
	}
	
	public Map<Character, ArrayList<String>> chomsky(Map<Character, ArrayList<String>> gramaticas){
		epsilon(gramaticas);                 //Paso 1: Eliminar epsilon producciones
		produccionesUnitarias(gramaticas);   //Paso 2: Eliminar producciones unitarias
		produccionesInutiles(gramaticas);    //Paso 3: Eliminar símboloes inútiles
		sustitucionTerminales(gramaticas);   //Paso 4: Sustituir Terminales 
		sustitucionNoTerminales(gramaticas); //Paso 5: Sustituir No Terminales
		
		return gramaticas;
	}
	
	private void epsilon(Map<Character, ArrayList<String>> gramaticas) {
		//Paso 1 Remover epsilons producciones 
		boolean epsilon = false;
		ArrayList<Character> reemplazables = new ArrayList<>(); //Contiene epsilon
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones  =  entry.getValue();
			boolean cadenaVacia = false;
			for(String produccion : producciones) {
				if(produccion.charAt(0)=='e' && produccion.length()==1) {
					
					//Se encontro epsilon
					reemplazables.add(entry.getKey());
					producciones.remove(produccion);
					cadenaVacia = !cadenaVacia;
				}
				if(cadenaVacia)
					break;
			}
		}
		
		//Aqui agrega una nueva producción donde había Noterminales que producian cadena vacia
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> newList = new ArrayList<>();
			boolean newProduccionflag = false;
			
			for(String produccion : producciones) {
				StringBuilder newProduccion = new StringBuilder();
				for(int i=0;i<produccion.length();i++) {
					if(!reemplazables.contains(produccion.charAt(i))) {
						newProduccion.append(produccion.charAt(i));
					}else {
						newProduccionflag = true;
					}
				}if(newProduccionflag) {
					newList.add(newProduccion.toString());
					newProduccionflag = false;
				}
			}for(String str: newList) {
				producciones.add(str);
			}
		}
		
	}
	
	private void produccionesUnitarias (Map<Character, ArrayList<String>> gramaticas){
		
		//Eliminar noTerminales que esten solos y añadir sus producciones
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> reemplazo = new ArrayList<>();
			for(String produccion : producciones) {
				if(produccion.charAt(0)>64 && produccion.charAt(0)<91 && produccion.length()==1) {
					ArrayList<String> reemplazador = gramaticas.get(produccion.charAt(0));
					producciones.remove(produccion);
					reemplazo.addAll(reemplazador);
				}
			}reemplazo.addAll(producciones);
			gramaticas.put(entry.getKey(), reemplazo);
		}
	}
	
	private void produccionesInutiles (Map<Character, ArrayList<String>> gramaticas) {
		ArrayList<Character> inutiles = new ArrayList<>();
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			for(String produccion : producciones) {
				if((produccion.charAt(0) == entry.getKey().charValue() && produccion.length()==1) || (produccion.charAt(0) == entry.getKey().charValue() && produccion.charAt(1) == entry.getKey().charValue())){
					inutiles.add(entry.getKey());
				}
			}
		}
		
		for(Character caracter : inutiles) {//Eliminar noterminales inutiles
			gramaticas.remove(caracter);
		}
		
		//Modificando producciones para que no esten con un simbolo inutil
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> produccionesUtiles = new ArrayList<>(); 
			boolean remove = false;
			if(inutiles.contains(entry.getKey())) {
				remove = true;
			}else {
				for(String produccion : producciones) {
					boolean util = true; 
					for(int i = 0;i<produccion.length();i++) {
						if(inutiles.contains(produccion.charAt(i))){
							util = false;
						}
					}
					
					if(util) {
						produccionesUtiles.add(produccion);
					}
				}
			}
				gramaticas.put(entry.getKey(), produccionesUtiles);
		}
		
		//Por si alguna gramatica ya no se manda a llamar porque estaba concatenada con una inutil
		Set<Character> simbolos = new HashSet<>(gramaticas.keySet());
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			for(String produccion : producciones) {
				 for(int  i = 0; i<produccion.length();i++) {
					 if(simbolos.contains(produccion.charAt(i))){
						 simbolos.remove(produccion.charAt(i));
					 }
				}
			}
		}
		
		for (Character simbol : simbolos) {
	        gramaticas.remove(simbol);
	     }
	}
	
	private void sustitucionTerminales(Map<Character, ArrayList<String>> gramaticas) {
		Set<Character> produccionesAnteriores = new HashSet<>();
		
		int simbolsBegin = 65;
		
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			if(!produccionesAnteriores.contains(entry.getKey())) {
				produccionesAnteriores.add(entry.getKey());
			}
		}
		
		//Nuevas Producciones
		Map<Character, Character> terminalesProducciones = new Hashtable<>();
		Map<Character, ArrayList<String>> nuevasPro = new Hashtable<>();
		
		//Sustitucion de terminales y moverlas al lado derecho
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> reemplazoATerminales = new ArrayList<>();
			for(String produccion: producciones) {
				for(int i=0;i<produccion.length();i++) {
					if(produccion.charAt(i)>=97) {//terminal
						char c = (char)simbolsBegin;
						if(!terminalesProducciones.containsKey(produccion.charAt(i))) {
							while(nuevasPro.containsKey(c) || produccionesAnteriores.contains(c)) {//Encontrar letra no terminal
								simbolsBegin++;
								c = (char)simbolsBegin;	
							}
							terminalesProducciones.put(produccion.charAt(i), c);
							ArrayList<String> terminal = new ArrayList<>();
							terminal.add(produccion.charAt(i)+"");
							nuevasPro.put(c, terminal);
							produccion = produccion.replace(produccion.charAt(i),c);
						}else{
							produccion = produccion.replace(produccion.charAt(i),terminalesProducciones.get(produccion.charAt(i))); 
						}
					}
				}reemplazoATerminales.add(produccion);
			}gramaticas.put(entry.getKey(), reemplazoATerminales);
		}gramaticas.putAll(nuevasPro);
	}
	
	private void sustitucionNoTerminales(Map<Character, ArrayList<String>> gramaticas) {
		Map<Character, ArrayList<String>> produccionesNoTerminales =  new Hashtable<>();
		
		int simbols = 65; //Código Ascii
		
		for (Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();;
			ArrayList<String> nuevaProduccion = new ArrayList<String>();
			for(String produccion : producciones) {
				int numNoTerminales= 0;
				boolean flag = false;
				for(int i=produccion.length()-1;i>=0;i--) {
					if(flag) {
						i=produccion.length()-1;
						flag = false;
					}
					if(produccion.charAt(i)>64 && produccion.charAt(i)<91) {
						numNoTerminales++;
					}
					
					if(numNoTerminales==2 && (i-1)>=0) {//Nueva Produccion o Cambiar de 2 noTerminales a 1
						char c = (char)simbols;
						if(produccionesNoTerminales.containsValue(produccion.substring(i, i+2))) {
							boolean encontrado=false;
							 for(Map.Entry<Character, ArrayList<String>> iterator : produccionesNoTerminales.entrySet()) {
								 if(encontrado)
									 break;
								 ArrayList<String> lista = entry.getValue();
								  for(String str : lista) {
									  if(str.equals(produccion.substring(i, i+2)));
									  encontrado = true;
								  }
							  }
							 
						}else {
							 c = (char)simbols;
							while(gramaticas.containsKey(c) || produccionesNoTerminales.containsKey(c)) {
								simbols++;
								c = (char)simbols;
							}
						}
						flag = true;
						ArrayList<String> str = new ArrayList<>(); //Produccion de dos terminales
						str.add(produccion.substring(i, i+2));
						produccionesNoTerminales.put(c, str);
						numNoTerminales=0;
						produccion = produccion.substring(0, i)+c;
					}
				}nuevaProduccion.add(produccion);
			}gramaticas.put(entry.getKey(), nuevaProduccion);
		}gramaticas.putAll(produccionesNoTerminales);
	}

	private boolean CYK(Map<Character, ArrayList<String>> gramaticas, String cadena) {
		ArrayList<ArrayList<ArrayList<Character>>> table = new  ArrayList<ArrayList<ArrayList<Character>>>();
		int num = cadena.length();
		for(int i=0;i<cadena.length()+1;i++) {//row
			ArrayList<ArrayList<Character>> columna =  new ArrayList<>();
			for(int j =0; j<num; j++ ) {//cadena
				
				
				if(i==0) {//Poner la cadena en el array 0
					ArrayList<Character> simbolos =  new ArrayList<>(); 
					simbolos.add(cadena.charAt(j));
					columna.add(simbolos);
				}else if(i==1){ //Primer Nivel asi que solo tendran un no Terminal
					ArrayList<Character> simbolosNuevos =  new ArrayList<>();
					for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
						ArrayList<String> producciones = entry.getValue(); 
						
						//Contador de caracteres en casillas anteriores
						String produccionTerminal = producciones.get(0);
						Character casillaAnterior = table.get(i-1).get(j).get(0);
						if(produccionTerminal.charAt(0)==casillaAnterior) {
							simbolosNuevos.add(entry.getKey());
							columna.add(simbolosNuevos);
							break;
						}
					}if(simbolosNuevos.isEmpty()) {
						return false;
					}
				}else {
					int m = 1; //Row de primera columna
					int n = i-1;//Row de columna en movimiento
					int x = j+1; //Avanza de columna
					ArrayList<Character> SimbolosNuevos = new ArrayList<>();
					boolean produccionEncontrada = false;
					
					while(m<i) {
						//Primero es iteracion de las dos columnas para ver sus combinaciones
						ArrayList<Character> column1 = table.get(m).get(j);
						ArrayList<Character> column2 = table.get(n).get(x);
						
						//Iteracion de las dos ArrayList
						for(Character c1 : column1) {
							for(Character c2: column2 ) {
								if(c1 !=null && c2!=null) {
									String combinacion = ""+c1+c2;
									
									//Iteración de gramaticas para saber si alguien produce la combinacion
									for(Map.Entry<Character, ArrayList<String>> entry :gramaticas.entrySet()) {
										ArrayList<String> noTerminal = entry.getValue();
										for(String produccion: noTerminal) {
											if(produccion.equals(combinacion)) {
												SimbolosNuevos.add(entry.getKey());
												produccionEncontrada = true;
											}
										}
									}
								}
							}
						}
						//Avance
						m++; n--; x++;
					}
					if(!produccionEncontrada) {
						SimbolosNuevos.add(null);
					}
					columna.add(SimbolosNuevos);
				}
					
			}table.add(columna);
			if(i>0)
				num--;
		}
		
		ArrayList<Character> VerificacionSimboloFinal = table.get(cadena.length()).get(0);
		
		for(Character c : VerificacionSimboloFinal) {
			if(c == null)
				return false;
			
			if(c == 'S') {
				Node<Character> node =  new Node<Character>('S');
				DerivationTree<Node<Character>> tree = new DerivationTree(node);
				arbolDerivacion(tree.root, table, table.size()-1, 0);
				System.out.println("Nivel 0: "+tree.root.getInfo());
				imprimirArbol(tree.root.left,tree.root.right,1);
				return true;
				
			} 
		}
		
		return false;
	}

	public void arbolDerivacion(Node root, ArrayList<ArrayList<ArrayList<Character>>> table, int row, int column) {
		
		Node<Character> tmp = root;
		Node<Character> tmp2 = root;
		root.setInfo(table.get(row).get(column).get(0));
		int terminales = 0;
		int indexR = row;
		int indexC =column;
		
		while(terminales<cadena.length()) {
			while(indexR>0) {
			    int	indexRtmp = indexR-1;
			    int indexCtmp = indexC+1;
			    if(tmp!=null) {
			    	tmp2 = tmp;
				    while(indexRtmp>0) {
						if(table.get(indexRtmp).get(indexCtmp).get(0)!=null) {
							tmp.right = new Node<Character>(table.get(indexRtmp).get(indexCtmp).get(0));
							tmp = tmp.getRight();
						}indexRtmp--;
						indexCtmp++;
				    }indexRtmp=indexR-1;
				    indexCtmp = indexC;
				    while(indexRtmp>0) {
				    	if(table.get(indexRtmp).get(indexCtmp).get(0)!=null) {
					    	tmp = tmp2;
					    	tmp.left = new Node<Character>(table.get(indexRtmp).get(indexCtmp).get(0));
					    	tmp = tmp.left;
					    	int fila =indexRtmp-1;
					    	int columna = indexCtmp+1;
					    	Node tmpRight = tmp;
					    	while(fila>0) {
					    		tmp2 = tmp; 
								if(table.get(fila).get(columna).get(0)!=null) {
									tmpRight.right = new Node<Character>(table.get(fila).get(columna).get(0));
									tmpRight = tmpRight.getRight();
								}fila--;
								columna++;
							 }
				    	}indexRtmp--;
				    }
			    }indexR--;
			    indexC++;
			    tmp = tmp2.right;
			    terminales++;
			}
		}
	}

	public void imprimirArbol(Node left, Node right, int nivel) {
		//Impresion por nivel separados por coma
		if(left!=null) {
			System.out.print("Nivel "+nivel+": "+left.getInfo()+", ");
			imprimirArbol(left.left, left.right, nivel+1);
		}
		
		if(right!=null) {
			System.out.print(right.getInfo()+"\n");
			imprimirArbol(right.left, right.right, nivel+1);
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/**Input: Gramática y Cadena*/
		Scanner sc = new Scanner(System.in); 
		/*String[] input = {"S-> aAB", //Ejemplo para hacer a chomsky
						  "A-> BAb|e|B",
						  "B-> a|b|CD",
						  "C-> ba",
						  "D-> DD"};*/
		
		String[] input = {"S-> aSb|e"}; 
		
		String cadena = "aaabbb"; //Cadena de prueba "aaabbb"
		
		CYK_Algorithm algoritmo =  new CYK_Algorithm(input, cadena);
		
	}
	
	private class DerivationTree<E>{
		private Node<E> root;
		
		public DerivationTree() {
			this.root = null;
		}
		
		public DerivationTree(Node<E> node) {
			this.root = node;
		}

		public Node<E> getRoot() {
			return root;
		}

		public void setRoot(Node<E> root) {
			this.root = root;
		}
		
		
	}

	private class Node<E> {
		 private E info;
		 private Node<E> left;
		 private Node<E> right;
			
		 
		 public Node(E info) {
			 this.info = info;
			 this.left = null;
			 this.right = null;
		 }


		public E getInfo() {
			return info;
		}


		public void setInfo(E info) {
			this.info = info;
		}


		public Node<E> getLeft() {
			return left;
		}


		public void setLeft(Node<E> left) {
			this.left = left;
		}


		public Node<E> getRight() {
			return right;
		}


		public void setRight(Node<E> right) {
			this.right = right;
		}
		 
		 
	}
}
