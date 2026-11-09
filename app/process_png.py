import struct
import zlib

def read_png(file_path):
    with open(file_path, 'rb') as f:
        signature = f.read(8)
        assert signature == b'\x89PNG\r\n\x1a\n'
        
        chunks = []
        while True:
            length_bytes = f.read(4)
            if not length_bytes:
                break
            length = struct.unpack('>I', length_bytes)[0]
            chunk_type = f.read(4)
            chunk_data = f.read(length)
            crc = f.read(4)
            chunks.append((chunk_type, chunk_data))
            if chunk_type == b'IEND':
                break
    return chunks

def parse_ihdr(chunks):
    for c_type, c_data in chunks:
        if c_type == b'IHDR':
            width, height, bit_depth, color_type, compression, filter_method, interlace = struct.unpack('>IIBBBBB', c_data)
            return width, height, bit_depth, color_type
    raise ValueError("No IHDR found")

def get_idat_data(chunks):
    data = b''
    for c_type, c_data in chunks:
        if c_type == b'IDAT':
            data += c_data
    return zlib.decompress(data)

print("Script template ready")
