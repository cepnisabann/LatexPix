const express = require('express');
const TeXZilla = require('texzilla');

const app = express();
const port = 3000;

// Middleware for parsing JSON request bodies
app.use(express.json());

// API endpoint for LaTeX to MathML conversion
app.post('/convert', (req, res) => {
  const latex = req.body.latex; // Assuming the LaTeX expression is sent in the request body

  try {
    const mathml = TeXZilla.toMathMLString(latex,false,false,false);
    res.send(mathml);
  } catch (error) {
    console.error('Error converting LaTeX to MathML:', error);
    res.status(500).send('Error converting LaTeX to MathML');
  }
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
