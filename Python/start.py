import networkx as nx

# Création d'un graphe ferroviaire
G = nx.DiGraph()
# Ajouter les gares et les tronçons (simplifié)
G.add_edge("Gare_A", "Gare_B", weight=1)
G.add_edge("Gare_B", "Gare_C", weight=1)
G.add_edge("Gare_C", "Gare_D", weight=1)
G.add_edge("Gare_A", "Gare_D", weight=2)

# Trouver les chemins possibles
all_paths = list(nx.all_simple_paths(G, source="Gare_A", target="Gare_D"))
print("Manchettes possibles :", all_paths)

# Fonction pour récupérer le chemin le plus court
def get_shortest_path(paths):
    shortest_path = min(paths, key=len)
    return shortest_path

# Récupérer le chemin le plus court
shortest_path = get_shortest_path(all_paths)
print("Chemin le plus court :", shortest_path)