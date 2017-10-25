# mlp backprop


import numpy as np
import matplotlib.pyplot as plt


def sigmoid(x):
    return 1./(1.+np.exp(-x))


class Node:

    def __init__(self, isinput=False):
        self.out = 0.
        self.error = 0.
        self.x = None
        self.isinput = isinput


def is_input_layer(layer):
    return layer == 0


def compute_forward_input_nodes(x, nodes, layer, output_node):
    nodes[layer][output_node].out = x[output_node]


def compute_output_layer(nodes, layer, output_node, w):
    sum = 0.
    prev_layer = len(nodes[layer - 1])
    for input_node in range(prev_layer):
        weight = w[layer][output_node][input_node]
        input = nodes[layer - 1][input_node].out
        sum += weight * input
        # print_summing_line_info(layer, output_node, input_node)
    nodes[layer][output_node].out = sigmoid(sum)
    # print_ouput_line_info(layer, output_node, nodes)


def compute_layer(nodes, layer, x, w):
    # print('-------Layer:'+str(layer)+'---------------')
    current_layer = len(nodes[layer])
    for output_node in range(current_layer):
        if is_input_layer(layer):
            compute_forward_input_nodes(x, nodes, layer, output_node)
        else:
            compute_output_layer(nodes, layer, output_node, w)


def forward(x, w, nodes):
    layers = len(nodes)
    for layer in range(layers):
        compute_layer(nodes, layer, x, w)


def print_summing_line_info(layer, output_node_index, input_node_index):
    output = [
        'Summing:w[' + str(layer) + ']',
        '[' + str(output_node_index) + ']',
        '[' + str(input_node_index) + ']',
        '*N[' + str(layer - 1) + '][' + str(input_node_index) + '].out']
    print(''.join(output))


def print_output_line_info(layer, output_node_index, nodes):
    output = [
        'N[' + str(layer) + '][' + str(output_node_index) + '].out=',
        str(nodes[layer][output_node_index].out)]
    print(''.join(output))


def print_backward_base_error_info(layer, output_node_index):
    output = [
        'p1=N[' + str(layer) + '][' + str(output_node_index) + '].out',
        '*(1-N[' + str(layer) + '][' + str(output_node_index) + '].out)']
    print(''.join(output))


def print_backward_error_output_layer(layer, output_node_index):
    output = [
        'N[' + str(layer) + ']',
        '[' + str(output_node_index) + '].error',
        '= p1*(t-', 'N[' + str(layer) + ']',
        '[' + str(output_node_index) + '].out)']
    print(''.join(output))


def print_backward_error_sum(layer, input_node, output_node):
    output = [
        'sum += w[' + str(layer + 1) + ']',
        '[' + str(input_node) + ']',
        '[' + str(output_node) + ']',
        '*N[' + str(layer + 1) + ']',
        '[' + str(input_node) + '].error']
    print(''.join(output))


def print_backward_error_hidden_layer(layer, output_node):
    print('N['+str(layer)+']['+str(output_node)+'].error = p1*sum')


def compute_error_output_layer(nodes, layer, output_node, p1, t, y):
    nodes[layer][output_node].error = p1 * (t - y)
    # print_backward_error_output_layer(layer, output_node)


def compute_error_hidden_layer(nodes, layer, node, w, p1):
    sum = 0.
    for input_node in range(len(nodes[layer+1])):
        for output_node in range(len(nodes[layer])):
            wkh = w[layer+1][input_node][output_node]
            deltah = nodes[layer + 1][input_node].error
            sum += wkh * deltah
            # print_backward_error_sum(layer, input_node, output_node)
    # gives better results?? why?
    nodes[layer][output_node].error = p1 * sum
    # this is the proper way, but gives worser results!
    #nodes[layer][node].error = p1 * sum
    # print_backward_error_hidden_layer(layer, node)


def backward(nodes, t, w):
    number_of_layers = len(nodes)
    for layer in range(number_of_layers-1, 0, -1):
        # print('-------Layer backtracking:'+str(layer)+'---------------')
        number_of_nodes_current_layer = len(nodes[layer])
        for node in range(number_of_nodes_current_layer):
            y = nodes[layer][node].out
            p1 = y*(1-y)
            # print_backward_base_error_info(layer, node)
            if layer == len(nodes)-1:
                compute_error_output_layer(nodes, layer, node, p1, t, y)
            else:
                compute_error_hidden_layer(nodes, layer, node, w, p1)


def print_delta_weight_info(layer, output_node, input_node):
    output = [
        'delta_w = ',
        'alfa*N[' + str(layer) + '][' + str(output_node) + '].error',
        '*N[' + str(layer - 1) + '][' + str(input_node) + '].out']
    print(''.join(output))


def print_weight_recompute_info(layer, output_node, input_node):
    output = [
        'w[' + str(layer) + ']',
        '[' + str(output_node) + ']',
        '[' + str(input_node) + ']',
        '+= delta_w']
    print(''.join(output))


def compute_weight(nodes, layer, output_node, input_node, w, alfa):
    delta_h = nodes[layer][output_node].error
    y = nodes[layer - 1][input_node].out
    delta_w = alfa * delta_h * y
    w[layer][output_node][input_node] += delta_w


def recompute_weights(nodes, w, alfa):
    for layer in range(len(nodes)):
        # print('-------Layer recomputing weights:'+str(layer)+'------------')
        if layer == 0:
            continue
        for output_node in range(len(nodes[layer])):
            for input_node in range(len(nodes[layer-1])):
                compute_weight(nodes, layer, output_node, input_node, w, alfa)
                # print_delta_weight_info(layer, output_node, input_node)
                # print_weight_recompute_info(layer, output_node, input_node)


def init_input_weights(w):
    w.append([])


def init_hidden_weights(w, num_in, list_num_hid, w_min, w_max):
    num = num_in
    for k_hid in range(len(list_num_hid)):
        w_hid = []
        for k in range(list_num_hid[k_hid]):
            w_hid.append(np.random.uniform(w_min, w_max, num))
        w.append(w_hid)
        num = list_num_hid[k_hid]


def init_output_weights(w, num_out, num_in, w_min, w_max):
    w_out = []
    for k in range(num_out):
        w_out.append(np.random.uniform(w_min, w_max, num_in))
    w.append(w_out)


def init_weights(num_in, list_num_hid, num_out):
    w_min = 0.
    w_max = 1.
    w = []

    init_input_weights(w)
    init_hidden_weights(w, num_in, list_num_hid, w_min, w_max)
    init_output_weights(w, num_out, list_num_hid[-1], w_min, w_max)

    return w


def init_input_nodes(nodes, num_in):
    nodes_in = []
    for i in range(num_in):
        nodes_in.append(Node(True))
    nodes.append(nodes_in)


def init_hidden_nodes(nodes, list_num_hid):
    for k in range(len(list_num_hid)):
        nodes_hidden = []
        for i in range(list_num_hid[k]):
            nodes_hidden.append(Node())
        nodes.append(nodes_hidden)


def init_output_nodes(nodes, num_out):
    nodes_out = []
    for i in range(num_out):
        nodes_out.append(Node())
    nodes.append(nodes_out)


def init_nodes(num_in, list_num_hid, num_out):
    nodes = []

    init_input_nodes(nodes, num_in)
    init_hidden_nodes(nodes, list_num_hid)
    init_output_nodes(nodes, num_out)

    return nodes


def global_error_rises(global_error):
    return len(global_error) > 2 and global_error[-2] < global_error[-1]


def stop_condition(global_error, it):
    return global_error_rises(global_error) or \
           global_error[-1] < 0.001 or \
           it > 100000


def train_network(x, w, t, nodes, alfa):
    it = 0
    global_error = []
    while True:
        e = 0.
        # takze indexi su [layer][output_node_index][input_node_index]
        for index_input in range(len(x)):

            forward(x[index_input], w, nodes)
            backward(nodes, t[index_input], w)
            recompute_weights(nodes, w, alfa)

            e += np.power(t[index_input] - nodes[-1][0].out, 2)

        e = 0.5*e
        global_error.append(e)
        it = it + 1
        if stop_condition(global_error, it):
            break

    print('global_error = '+str(global_error[-1]))
    print('Iterations='+str(it))

    return global_error


def compute_network(x, w, nodes):
    result = []
    for i in range(len(x)):
        forward(x[i], w, nodes)
        result.append(nodes[-1][0].out)

    return result


def print_results(t, result):
    print('RESULTS:')
    for i in range(len(t)):
        print(str(t[i])+'->'+str(result[i]))


def main():
    x = [[0., 0.], [0., 1.], [1., 0.], [1., 1.]]
    t = [0., 1., 1., 1.]

    # after ~20000 iterations doesn't get better & big error
    # w = init_weights(2, [2, 4], 1)
    # nodes = init_nodes(2, [2, 4], 1)

    # after ~20000 iterations doesn't get better & big error
    # w = init_weights(2, [2, 4, 2], 1)
    # nodes = init_nodes(2, [2, 4, 2], 1)

    # after ~20000 iterations doesn't get better
    # alfa = 0.01, global_error = 0.282287335698
    # alfa = 0.1, global_error = 0.281951601816
    # w = init_weights(2, [4], 1)
    # nodes = init_nodes(2, [4], 1)

    # after ~20000 iterations doesn't get better
    w = init_weights(2, [2, 4], 1)
    print(w)
    nodes = init_nodes(2, [2, 4], 1)

    alfa = 0.1

    global_error = train_network(x, w, t, nodes, alfa)
    result = compute_network(x, w, nodes)
    print_results(t, result)

    plt.plot(range(len(global_error)), global_error, 'r')
    plt.show()


if __name__ == '__main__':
    main()
