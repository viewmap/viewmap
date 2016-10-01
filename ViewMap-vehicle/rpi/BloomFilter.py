class BloomFilter:
    def __init__(self, num_bytes, num_probes, iterable=()):
        self.array = bytearray(num_bytes)
        self.num_probes = num_probes
        self.num_bins = num_bytes * 8
        self.update(iterable)
    def get_probes(self, key):
        random = Random(key).random
        return (int(random() * self.num_bins) for _ in range(self.num_probes))
    def update(self, keys):
        for key in keys:
            for i in self.get_probes(key):
                self.array[i//8] |= 2 ** (i%8)
    def select(self, key):
        return all(self.array[i//8] & (2 ** (i%8)) for i in self.get_probes(key))