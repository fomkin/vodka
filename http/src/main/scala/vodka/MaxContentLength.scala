package vodka

class MaxContentLength
    extends Exception("Max content length reached")
    with BadRequestException
