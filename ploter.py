import csv
import matplotlib.pyplot as plt


def getKey(item):
    return item[1]

data = open('results.csv', 'rb')
reader = csv.reader(data, delimiter=',')
uniform25 = []
normal25 = []
uniform50 = []
normal50 = []
uniform75 = []
normal75 = []
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


uniform25 = [x[0] for x in sorted(uniform25, key=getKey)]
uniform50 = [x[0] for x in sorted(uniform50, key=getKey)]
uniform75 = [x[0] for x in sorted(uniform75, key=getKey)]
normal25 = [x[0] for x in normal25[0:len(normal25)-1]]
normal50 = [x[0] for x in sorted(normal50[0:len(normal50)-1], key=getKey)]
normal75 = [x[0] for x in sorted(normal75, key=getKey)]
numbers = range(9, 22)

plt.plot(numbers, normal25, 'b--', label='Normal 0.25')
plt.plot(numbers, normal50, 'bs', label='Normal 0.50')
plt.plot(numbers, normal75, 'b^', label='Normal 0.75')
plt.plot(numbers, uniform25, 'g--', label='Uniforme 0.25')
plt.plot(numbers, uniform50, 'gs', label='Uniforme 0.50')
plt.plot(numbers, uniform25, 'g^', label='Uniforme 0.75')
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
plt.xlabel('Orden de potencia de 2 de segmentos')
plt.ylabel('Numero de acceso a discos')
plt.savefig('all.png', bbox_inches='tight')
plt.clf()

plt.plot(numbers, normal25, 'b--', label='Normal 0.25')
plt.plot(numbers, uniform25, 'g--', label='Uniforme 0.25')
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
plt.xlabel('Orden de potencia de 2 de segmentos')
plt.ylabel('Numero de acceso a discos')
plt.savefig('25.png')
plt.clf()

plt.plot(numbers, normal50, 'b--', label='Normal 0.50')
plt.plot(numbers, uniform50, 'g--', label='Uniforme 0.50')
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
plt.xlabel('Orden de potencia de 2 de segmentos')
plt.ylabel('Numero de acceso a discos')
plt.savefig('50.png')
plt.clf()

plt.plot(numbers, normal75, 'b--', label='Normal 0.75')
plt.plot(numbers, uniform75, 'g--', label='Uniforme 0.75')
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3, ncol=2, mode="expand", borderaxespad=0.)
plt.xlabel('Orden de potencia de 2 de segmentos')
plt.ylabel('Numero de acceso a discos')
plt.savefig('75.png')
plt.clf()



