import csv
import matplotlib.pyplot as plt


# Auxiliary function to get the second value of a tuple
def getKey(item):
    return item[1]


# Ploter for the results
def plot(coord):
    data = open('results'+coord+'.csv', 'rb')
    reader = csv.reader(data, delimiter=',')
    uniform25 = list()
    normal25 = list()
    uniform50 = list()
    normal50 = list()
    uniform75 = list()
    normal75 = list()

    # Separate the results in corresponding distribution and proportion
    for row in reader:
        file = row[0]
        value = row[1]
        att = file.split('-')
        if att[3] == "normal":
            if att[4] == "0.25.csv":
                normal25.append([int(value), int(att[2])])
            elif att[4] == "0.5.csv":
                normal50.append([int(value), int(att[2])])
            else:
                normal75.append([int(value), int(att[2])])
        else:
            if att[4] == "0.25.csv":
                uniform25.append([int(value), int(att[2])])
            elif att[4] == "0.5.csv":
                uniform50.append([int(value), int(att[2])])
            else:
                uniform75.append([int(value), int(att[2])])

    #Remove repeated values
    old_uniform25 = uniform25
    uniform25 = []
    old_uniform50 = uniform50
    uniform50 = []
    old_uniform75 = uniform75
    uniform75 = []

    old_normal25 = normal25
    normal25 = []
    old_normal50 = normal50
    normal50 = []
    old_normal75 = normal75
    normal75 = []

    for x in old_uniform25:
        if x not in uniform25:
            uniform25.append(x)

    for x in old_uniform50:
        if x not in uniform50:
            uniform50.append(x)

    for x in old_uniform75:
        if x not in uniform75:
            uniform75.append(x)

    for x in old_normal25:
        if x not in normal25:
            normal25.append(x)

    for x in old_normal50:
        if x not in normal50:
            normal50.append(x)

    for x in old_normal75:
        if x not in normal75:
            normal75.append(x)

    # Sort values and only save accesses
    uniform25 = [x[0] for x in sorted(uniform25, key=getKey)]
    uniform50 = [x[0] for x in sorted(uniform50, key=getKey)]
    uniform75 = [x[0] for x in sorted(uniform75, key=getKey)]
    normal25 = [x[0] for x in sorted(normal25, key=getKey)]
    normal50 = [x[0] for x in sorted(normal50, key=getKey)]
    normal75 = [x[0] for x in sorted(normal75, key=getKey)]
    numbers = range(9, 22)

    # Plot comparing all the tests
    plt.plot(numbers, normal25, 'b--', label='Normal 0.25')
    plt.plot(numbers, normal50, 'bs', label='Normal 0.50')
    plt.plot(numbers, normal75, 'b^', label='Normal 0.75')
    plt.plot(numbers, uniform25, 'g--', label='Uniforme 0.25')
    plt.plot(numbers, uniform50, 'gs', label='Uniforme 0.50')
    plt.plot(numbers, uniform25, 'g^', label='Uniforme 0.75')
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
    plt.xlabel('Orden de potencia de 2 de segmentos')
    plt.ylabel('Numero de acceso a discos')
    plt.savefig('all'+coord+'.png', bbox_inches='tight')
    plt.clf()

    # Plot comparing test with alpha = 0.25
    plt.plot(numbers, normal25, 'b--', label='Normal 0.25')
    plt.plot(numbers, uniform25, 'g--', label='Uniforme 0.25')
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
    plt.xlabel('Orden de potencia de 2 de segmentos')
    plt.ylabel('Numero de acceso a discos')
    plt.savefig('25'+coord+'.png')
    plt.clf()

    # Plot comparing test with alpha = 0.50
    plt.plot(numbers, normal50, 'b--', label='Normal 0.50')
    plt.plot(numbers, uniform50, 'g--', label='Uniforme 0.50')
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
    plt.xlabel('Orden de potencia de 2 de segmentos')
    plt.ylabel('Numero de acceso a discos')
    plt.savefig('50'+coord+'.png')
    plt.clf()

    # Plot comparing test with alpha = 0.75
    plt.plot(numbers, normal75, 'b--', label='Normal 0.75')
    plt.plot(numbers, uniform75, 'g--', label='Uniforme 0.75')
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
    plt.xlabel('Orden de potencia de 2 de segmentos')
    plt.ylabel('Numero de acceso a discos')
    plt.savefig('75'+coord+'.png')
    plt.clf()

# Plot for x and y tests
plot('x')
plot('y')
